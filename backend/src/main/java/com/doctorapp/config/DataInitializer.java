package com.doctorapp.config;

import com.doctorapp.entity.*;
import com.doctorapp.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           DoctorProfileRepository doctorProfileRepository,
                           PatientProfileRepository patientProfileRepository,
                           AvailabilitySlotRepository availabilitySlotRepository,
                           AppointmentRepository appointmentRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Data already seeded
        }

        System.out.println(">>> Initializing Doctor Appointment Booking demo data...");

        // 1. Seed Demo Doctors
        DoctorProfile docSarah = createDoctor(
                "doctor@demo.com",
                "password123",
                "Dr. Sarah Jenkins",
                "+1 (555) 234-5678",
                "Cardiology",
                "MBBS, MD (Cardiology), FACC",
                12,
                75.0,
                "Manhattan Heart & Vascular Center, Suite 402, New York, NY",
                "Board-certified Cardiologist specializing in preventive heart health, hypertension, coronary artery disease, and advanced cardiac diagnostics.",
                "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=400&auto=format&fit=crop&q=80",
                4.9,
                142
        );

        DoctorProfile docChen = createDoctor(
                "drchen@demo.com",
                "password123",
                "Dr. David Chen",
                "+1 (555) 345-6789",
                "Dermatology",
                "MD (Dermatology), FAAD",
                9,
                65.0,
                "Downtown Skin & Aesthetic Clinic, 5th Floor, New York, NY",
                "Specialist in clinical and cosmetic dermatology, acne treatment, eczema, skin cancer screening, and laser therapies.",
                "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=400&auto=format&fit=crop&q=80",
                4.8,
                98
        );

        DoctorProfile docEmily = createDoctor(
                "dremily@demo.com",
                "password123",
                "Dr. Emily Rodriguez",
                "+1 (555) 456-7890",
                "Pediatrics",
                "MD (Pediatrics), FAAP",
                14,
                55.0,
                "Sunshine Children's Health Center, Brooklyn, NY",
                "Compassionate pediatrician with 14+ years dedicated to newborn care, child development milestones, vaccinations, and adolescent wellness.",
                "https://images.unsplash.com/photo-1594824813633-820875e533c3?w=400&auto=format&fit=crop&q=80",
                4.95,
                210
        );

        DoctorProfile docMichael = createDoctor(
                "docmichael@demo.com",
                "password123",
                "Dr. Michael Thompson",
                "+1 (555) 567-8901",
                "Orthopedics",
                "MS (Orthopedic Surgery), FRCS",
                16,
                90.0,
                "Advanced Joint & Spine Hospital, East Wing, New York, NY",
                "Expert orthopedic surgeon focusing on sports injuries, knee and hip arthroplasty, and minimally invasive spine treatments.",
                "https://images.unsplash.com/photo-1537368910025-700350fe46c7?w=400&auto=format&fit=crop&q=80",
                4.9,
                167
        );

        DoctorProfile docPriya = createDoctor(
                "docpriya@demo.com",
                "password123",
                "Dr. Priya Patel",
                "+1 (555) 678-9012",
                "General Medicine",
                "MBBS, MD (Internal Medicine)",
                8,
                45.0,
                "Metro Care Family Clinic, Queens, NY",
                "Holistic primary care physician managing chronic health conditions, diabetes, seasonal illnesses, and routine annual physicals.",
                "https://images.unsplash.com/photo-1651008376811-b90baee60c1f?w=400&auto=format&fit=crop&q=80",
                4.85,
                115
        );

        // 2. Seed Demo Patients
        User patientUser = createPatient(
                "patient@demo.com",
                "password123",
                "John Doe",
                "+1 (555) 123-4567",
                "1994-05-14",
                "Male",
                "O+",
                "123 Maple Street, Apt 4B, Brooklyn, NY",
                "+1 (555) 987-6543 (Jane Doe)",
                "No known drug allergies. Mild seasonal pollen allergies."
        );

        User patientUser2 = createPatient(
                "emma@demo.com",
                "password123",
                "Emma Watson",
                "+1 (555) 234-9988",
                "1998-11-20",
                "Female",
                "A+",
                "456 Oak Avenue, Manhattan, NY",
                "+1 (555) 987-0011 (Mark Watson)",
                "Asthma (mild, controlled with inhaler)."
        );

        // 3. Seed Slots for each doctor for today and upcoming 5 days
        LocalDate today = LocalDate.now();
        List<DoctorProfile> allDocs = List.of(docSarah, docChen, docEmily, docMichael, docPriya);

        for (DoctorProfile doc : allDocs) {
            for (int dayOffset = 0; dayOffset <= 4; dayOffset++) {
                LocalDate slotDate = today.plusDays(dayOffset);
                seedSlotsForDoctor(doc, slotDate);
            }
        }

        // 4. Seed Demo Appointments
        // Upcoming Confirmed Appointment for John Doe with Dr. Sarah Jenkins
        Appointment apt1 = new Appointment(
                patientUser,
                docSarah,
                today.plusDays(1),
                "10:00",
                "10:30",
                "Annual routine heart checkup and blood pressure monitoring."
        );
        apt1.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(apt1);

        // Completed Past Appointment with Prescription
        Appointment apt2 = new Appointment(
                patientUser,
                docPriya,
                today.minusDays(5),
                "11:00",
                "11:30",
                "Persistent cough, fever, and mild sore throat for 3 days."
        );
        apt2.setStatus(AppointmentStatus.COMPLETED);
        apt2.setDoctorNotes("Patient presented with acute upper respiratory tract infection. Chest auscultation clear. Advised hydration and complete rest.");
        apt2.setPrescription("1. Amoxicillin 500mg - 1 capsule every 8 hours after food (5 days)\n2. Paracetamol 650mg - 1 tablet as needed for body ache/fever\n3. Warm saline gargles thrice daily");
        appointmentRepository.save(apt2);

        // Pending Request for Emma Watson with Dr. David Chen
        Appointment apt3 = new Appointment(
                patientUser2,
                docChen,
                today.plusDays(2),
                "14:00",
                "14:30",
                "Consultation regarding persistent skin rash and eczema flare-up."
        );
        apt3.setStatus(AppointmentStatus.PENDING);
        appointmentRepository.save(apt3);

        System.out.println(">>> Doctor Appointment Booking demo data seeded successfully!");
        System.out.println(">>> Demo Accounts ready:");
        System.out.println("    Doctor Account:  email: doctor@demo.com  | password: password123");
        System.out.println("    Patient Account: email: patient@demo.com | password: password123");
    }

    private DoctorProfile createDoctor(String email, String password, String name, String phone,
                                       String spec, String qual, int exp, double fee,
                                       String clinic, String bio, String imgUrl, double rating, int reviews) {
        User user = new User(email, passwordEncoder.encode(password), name, phone, Role.ROLE_DOCTOR);
        user = userRepository.save(user);

        DoctorProfile profile = new DoctorProfile(user, spec, qual, exp, fee, clinic, bio, imgUrl);
        profile.setRating(rating);
        profile.setTotalReviews(reviews);
        return doctorProfileRepository.save(profile);
    }

    private User createPatient(String email, String password, String name, String phone,
                              String dob, String gender, String blood, String address,
                              String emergency, String medHistory) {
        User user = new User(email, passwordEncoder.encode(password), name, phone, Role.ROLE_PATIENT);
        user = userRepository.save(user);

        PatientProfile profile = new PatientProfile(user, dob, gender, blood, address, emergency, medHistory);
        patientProfileRepository.save(profile);
        return user;
    }

    private void seedSlotsForDoctor(DoctorProfile doctor, LocalDate date) {
        String[] startTimes = {"09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                               "14:00", "14:30", "15:00", "15:30", "16:00", "16:30"};
        String[] endTimes   = {"09:30", "10:00", "10:30", "11:00", "11:30", "12:00",
                               "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"};

        List<AvailabilitySlot> slots = new ArrayList<>();
        for (int i = 0; i < startTimes.length; i++) {
            AvailabilitySlot slot = new AvailabilitySlot(doctor, date, startTimes[i], endTimes[i]);
            // If it matches our pre-seeded appointment apt1, mark booked
            if (date.equals(LocalDate.now().plusDays(1)) && startTimes[i].equals("10:00") && doctor.getUser().getEmail().equals("doctor@demo.com")) {
                slot.setBooked(true);
            }
            slots.add(slot);
        }
        availabilitySlotRepository.saveAll(slots);
    }
}
