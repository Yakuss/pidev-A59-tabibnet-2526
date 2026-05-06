<?php

namespace App\Controller;

use App\Entity\Patient;
use App\Entity\Medecin;
use App\Entity\Appointment;
use App\Repository\AppointmentRepository;
use App\Repository\MedecinRepository;
use App\Repository\PatientRepository;
use App\Service\WeatherService;
use App\Service\HolidayService;
use App\Service\AiSchedulingService;
use App\Service\AvailabilityService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

/**
 * IntelligenceController
 *
 * Exposes 3 REST endpoints consumed by the JavaFX doctor-calendar desktop app:
 *  - GET /api/intelligence/weather       → weather forecast for a location/date
 *  - GET /api/intelligence/holidays      → Tunisian national holidays for a year
 *  - GET /api/intelligence/recommendation/{patientId}/{doctorId}
 *                                         → AI best-slot recommendation
 *
 * No authentication guard is applied here so the Java HTTP client can reach
 * these endpoints without session cookies.
 */
#[Route('/api/intelligence')]
class IntelligenceController extends AbstractController
{
    // ──────────────────────────────────────────────────────────────────────────
    // WEATHER ENDPOINT
    // GET /api/intelligence/weather?location=Tunis&date=2026-05-10
    // ──────────────────────────────────────────────────────────────────────────

    #[Route('/weather', name: 'api_intelligence_weather', methods: ['GET'])]
    public function weather(Request $request, WeatherService $weatherService): JsonResponse
    {
        $location = $request->query->get('location', 'Tunis');
        $dateStr  = $request->query->get('date', date('Y-m-d'));

        try {
            $dateTime = new \DateTime($dateStr);
        } catch (\Exception $e) {
            return new JsonResponse(['error' => 'Invalid date format.'], 400);
        }

        $badWeather = $weatherService->getBadWeatherForecast($location, $dateTime);
        return new JsonResponse($this->formatWeatherResponse($badWeather));
    }

    #[Route('/week-weather', name: 'api_intelligence_week_weather', methods: ['GET'])]
    public function weekWeather(Request $request, WeatherService $weatherService): JsonResponse
    {
        $location = $request->query->get('location', 'Tunis');
        $startDateStr = $request->query->get('start_date', date('Y-m-d'));
        
        try {
            $startDate = new \DateTime($startDateStr);
        } catch (\Exception $e) {
            return new JsonResponse(['error' => 'Invalid date format.'], 400);
        }

        $results = [];
        for ($i = 0; $i < 7; $i++) {
            $date = (clone $startDate)->modify("+$i days");
            $dateStr = $date->format('Y-m-d');
            $badWeather = $weatherService->getBadWeatherForecast($location, $date);
            $results[$dateStr] = $this->formatWeatherResponse($badWeather);
        }

        return new JsonResponse($results);
    }

    private function formatWeatherResponse(?string $badWeather): array
    {
        $isRisky = $badWeather !== null;
        return [
            'condition'   => $isRisky ? 'bad' : 'clear',
            'description' => $isRisky ? $badWeather : 'Conditions favorables.',
            'isRisky'     => $isRisky
        ];
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HOLIDAYS ENDPOINT
    // GET /api/intelligence/holidays?year=2026
    // ──────────────────────────────────────────────────────────────────────────

    #[Route('/holidays', name: 'api_intelligence_holidays', methods: ['GET'])]
    public function holidays(Request $request, HolidayService $holidayService): JsonResponse
    {
        $year = (int) $request->query->get('year', date('Y'));

        if ($year < 2000 || $year > 2100) {
            return new JsonResponse(['error' => 'Invalid year.'], 400);
        }

        $holidays = $holidayService->getTunisianHolidays($year);

        // Normalize: always return an array of {date, localName, name}
        // The Nager API already returns this shape, but we re-map for safety.
        $normalized = array_map(function (array $h): array {
            return [
                'date'      => $h['date']      ?? '',
                'localName' => $h['localName'] ?? $h['name'] ?? '',
                'name'      => $h['name']      ?? '',
            ];
        }, $holidays);

        return new JsonResponse([
            'year'     => $year,
            'country'  => 'TN',
            'holidays' => $normalized,
        ]);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // AI RECOMMENDATION ENDPOINT
    // GET /api/intelligence/recommendation/{patientId}/{doctorId}
    // ──────────────────────────────────────────────────────────────────────────

    #[Route('/recommendation/{patientId}/{doctorId}', name: 'api_intelligence_recommendation', methods: ['GET'])]
    public function recommendation(
        int $patientId,
        int $doctorId,
        PatientRepository   $patientRepo,
        MedecinRepository   $medecinRepo,
        AvailabilityService $availabilityService,
        AiSchedulingService $aiService
    ): JsonResponse {
        /** @var Patient|null $patient */
        $patient = $patientRepo->find($patientId);
        if (!$patient) {
            return new JsonResponse(['error' => "Patient #$patientId not found."], 404);
        }

        /** @var Medecin|null $doctor */
        $doctor = $medecinRepo->find($doctorId);
        if (!$doctor) {
            return new JsonResponse(['error' => "Doctor #$doctorId not found."], 404);
        }

        // Get available 21-day slots for the doctor
        $slotsForAi  = $availabilityService->getAvailableSlots($doctor, new \DateTime('today'), 21);
        $closestSlot = null;

        if (empty($slotsForAi)) {
            // Fallback: find the very next available slot beyond the 21-day window
            $closestSlot = $availabilityService->getNextAvailableSlot($doctor, new \DateTime('today'));
        }

        // Call AI scheduling service (uses OpenRouter/LLaMA with fallback)
        $result = $aiService->getSmartSuggestions($patient, $doctor, $slotsForAi, $closestSlot);

        return new JsonResponse([
            'patientId'  => $patientId,
            'doctorId'   => $doctorId,
            'patientName'=> $patient->getFirstName() . ' ' . $patient->getLastName(),
            'doctorName' => 'Dr. ' . $doctor->getFirstName() . ' ' . $doctor->getLastName(),
            'specialty'  => $doctor->getSpecialty(),
            'recommendation'          => $result['recommendation']          ?? null,
            'attendance_probability'  => $result['attendance_probability']  ?? null,
            'suggested_slots'         => $result['suggested_slots']         ?? [],
        ]);
    }
}
