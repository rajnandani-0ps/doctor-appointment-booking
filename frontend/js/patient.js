/**
 * Patient Dashboard logic for Doctor Appointment Booking System
 */

let selectedDoctor = null;
let selectedSlotTime = null;

document.addEventListener('DOMContentLoaded', () => {
    checkPatientAuth();
    loadSpecializations();
    loadDoctors();
    loadMyAppointments();
    initBookingForm();
    initPatientProfile();
});

function checkPatientAuth() {
    const user = getCurrentUser();
    if (!user) {
        window.location.href = 'login.html';
        return;
    }
    const welcomeSpan = document.getElementById('patient-welcome-name');
    if (welcomeSpan) {
        welcomeSpan.innerText = user.fullName || user.email;
    }
}

// -------------------------------------------------------------
// Doctor Directory & Search
// -------------------------------------------------------------
async function loadSpecializations() {
    try {
        const res = await request('/doctors/specializations');
        const select = document.getElementById('filter-specialization');
        if (select && res && res.data) {
            select.innerHTML = '<option value="">All Specializations</option>';
            res.data.forEach(spec => {
                const opt = document.createElement('option');
                opt.value = spec;
                opt.innerText = spec;
                select.appendChild(opt);
            });
        }
    } catch (err) {
        console.error('Failed to load specializations:', err);
    }
}

async function loadDoctors(search = '', specialization = '') {
    const grid = document.getElementById('doctors-grid');
    if (!grid) return;

    grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: #64748b;">Loading doctors...</div>';

    try {
        let url = '/doctors';
        const params = new URLSearchParams();
        if (search) params.append('search', search);
        if (specialization) params.append('specialization', specialization);
        if (params.toString()) url += `?${params.toString()}`;

        const res = await request(url);
        if (!res || !res.data || res.data.length === 0) {
            grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: #64748b;">No doctors found matching your criteria.</div>';
            return;
        }

        grid.innerHTML = res.data.map(doc => renderDoctorCard(doc)).join('');
    } catch (err) {
        grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: #dc2626;">Error: ${err.message}</div>`;
    }
}

function renderDoctorCard(doc) {
    const avatar = doc.profileImageUrl || 'https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=300&auto=format&fit=crop&q=80';
    const rating = doc.rating ? Number(doc.rating).toFixed(1) : '5.0';

    return `
        <div class="doctor-card">
            <div class="doctor-card-header">
                <img src="${avatar}" alt="${doc.fullName}" class="doctor-avatar" onerror="this.src='https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=300&auto=format&fit=crop&q=80'">
                <div class="doctor-info">
                    <h3>${doc.fullName}</h3>
                    <span class="specialization-tag">${doc.specialization}</span>
                    <div class="doctor-rating">
                        ★ ${rating} <span style="color: #64748b; font-size: 0.75rem;">(${doc.totalReviews || 0} reviews)</span>
                    </div>
                </div>
            </div>
            <div class="doctor-card-body">
                <div>
                    <div class="doctor-meta-row">
                        <span><strong>Exp:</strong> ${doc.experienceYears || 0} years</span>
                        <span class="doctor-fee">$${doc.consultationFee || 0}</span>
                    </div>
                    <div class="doctor-bio">${doc.bio || 'General medical practitioner with patient-first healthcare.'}</div>
                    <div class="doctor-address">
                        <span>📍</span><span>${doc.clinicAddress || 'Healthcare Clinic'}</span>
                    </div>
                </div>
                <button onclick="openBookingModal(${doc.id}, '${doc.fullName.replace(/'/g, "\\'")}', '${doc.specialization}', ${doc.consultationFee})" class="btn btn-primary btn-block">
                    Book Appointment
                </button>
            </div>
        </div>
    `;
}

function handleSearch(e) {
    e.preventDefault();
    const search = document.getElementById('search-input').value.trim();
    const spec = document.getElementById('filter-specialization').value;
    loadDoctors(search, spec);
}

// -------------------------------------------------------------
// Booking Modal & Slots
// -------------------------------------------------------------
async function openBookingModal(doctorId, doctorName, specialization, fee) {
    selectedDoctor = { id: doctorId, name: doctorName, specialization, fee };
    selectedSlotTime = null;

    document.getElementById('modal-doctor-name').innerText = doctorName;
    document.getElementById('modal-doctor-spec').innerText = specialization;
    document.getElementById('modal-doctor-fee').innerText = `$${fee}`;

    // Set min date to today
    const dateInput = document.getElementById('booking-date');
    const today = new Date().toISOString().split('T')[0];
    dateInput.min = today;
    dateInput.value = today;

    await loadDoctorSlots(doctorId, today);

    document.getElementById('booking-modal').classList.add('open');
}

function closeBookingModal() {
    document.getElementById('booking-modal').classList.remove('open');
    selectedDoctor = null;
    selectedSlotTime = null;
}

async function onDateChange() {
    if (!selectedDoctor) return;
    const date = document.getElementById('booking-date').value;
    await loadDoctorSlots(selectedDoctor.id, date);
}

async function loadDoctorSlots(doctorId, date) {
    const container = document.getElementById('slots-grid');
    if (!container) return;

    container.innerHTML = '<span style="grid-column: 1/-1; font-size: 0.85rem; color: #64748b;">Loading available slots...</span>';
    selectedSlotTime = null;

    try {
        const res = await request(`/doctors/${doctorId}/slots?date=${date}`);
        if (!res || !res.data || res.data.length === 0) {
            container.innerHTML = '<span style="grid-column: 1/-1; font-size: 0.85rem; color: #dc2626;">No slots available for this date.</span>';
            return;
        }

        container.innerHTML = res.data.map(slot => `
            <button type="button" 
                    class="slot-btn ${slot.booked ? 'booked' : ''}" 
                    ${slot.booked ? 'disabled' : ''}
                    onclick="selectSlot(this, '${slot.startTime}')">
                ${slot.startTime}
            </button>
        `).join('');
    } catch (err) {
        container.innerHTML = `<span style="grid-column: 1/-1; font-size: 0.85rem; color: #dc2626;">Error loading slots: ${err.message}</span>`;
    }
}

function selectSlot(button, time) {
    document.querySelectorAll('.slot-btn').forEach(btn => btn.classList.remove('selected'));
    button.classList.add('selected');
    selectedSlotTime = time;
}

function initBookingForm() {
    const form = document.getElementById('booking-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        if (!selectedDoctor || !selectedSlotTime) {
            showToast('Please select an available time slot.', 'error');
            return;
        }

        const date = document.getElementById('booking-date').value;
        const reason = document.getElementById('booking-reason').value.trim();
        const submitBtn = document.getElementById('confirm-booking-btn');

        submitBtn.disabled = true;
        submitBtn.innerText = 'Confirming...';

        try {
            const payload = {
                doctorId: selectedDoctor.id,
                appointmentDate: date,
                startTime: selectedSlotTime,
                reason: reason || 'General Consultation'
            };

            const res = await request('/appointments/book', {
                method: 'POST',
                body: JSON.stringify(payload)
            });

            if (res && res.success) {
                showToast('Appointment booked successfully!', 'success');
                closeBookingModal();
                loadMyAppointments();
                switchTab('appointments');
            }
        } catch (err) {
            showToast(err.message || 'Failed to book appointment.', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerText = 'Confirm Booking';
        }
    });
}

// -------------------------------------------------------------
// My Appointments Tab
// -------------------------------------------------------------
async function loadMyAppointments() {
    const tbody = document.getElementById('appointments-table-body');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: #64748b;">Loading appointments...</td></tr>';

    try {
        const res = await request('/appointments/my-appointments');
        if (!res || !res.data || res.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: #64748b;">No appointments booked yet.</td></tr>';
            return;
        }

        tbody.innerHTML = res.data.map(apt => {
            const statusClass = `badge-${apt.status.toLowerCase()}`;
            const isCompleted = apt.status === 'COMPLETED';
            const isCancelable = apt.status === 'PENDING' || apt.status === 'CONFIRMED';

            return `
                <tr>
                    <td><strong>#${apt.id}</strong></td>
                    <td>
                        <div><strong>${apt.doctorName || 'Doctor'}</strong></div>
                        <span style="font-size: 0.8rem; color: #64748b;">${apt.doctorSpecialization || ''}</span>
                    </td>
                    <td>${apt.appointmentDate}</td>
                    <td>${apt.startTime} - ${apt.endTime}</td>
                    <td><span class="badge ${statusClass}">${apt.status}</span></td>
                    <td style="max-width: 200px; font-size: 0.85rem; color: #475569;">${apt.reason || 'Consultation'}</td>
                    <td>
                        <div style="display: flex; gap: 0.4rem; flex-wrap: wrap;">
                            ${isCompleted ? `
                                <button onclick="viewPrescriptionModal('${encodeURIComponent(JSON.stringify(apt))}')" class="btn btn-secondary btn-sm" title="View Prescription">
                                    📋 Prescription
                                </button>
                            ` : ''}
                            ${isCancelable ? `
                                <button onclick="cancelAppointment(${apt.id})" class="btn btn-danger btn-sm">
                                    Cancel
                                </button>
                            ` : ''}
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: #dc2626;">Error: ${err.message}</td></tr>`;
    }
}

async function cancelAppointment(appointmentId) {
    if (!confirm('Are you sure you want to cancel this appointment?')) return;

    try {
        const res = await request(`/appointments/${appointmentId}/cancel`, { method: 'POST' });
        if (res && res.success) {
            showToast('Appointment cancelled successfully.', 'success');
            loadMyAppointments();
        }
    } catch (err) {
        showToast(err.message || 'Failed to cancel appointment', 'error');
    }
}

function viewPrescriptionModal(encodedApt) {
    const apt = JSON.parse(decodeURIComponent(encodedApt));
    document.getElementById('modal-rx-doctor').innerText = `${apt.doctorName} (${apt.doctorSpecialization})`;
    document.getElementById('modal-rx-date').innerText = apt.appointmentDate;
    document.getElementById('modal-rx-notes').innerText = apt.doctorNotes || 'No notes added.';
    document.getElementById('modal-rx-content').innerText = apt.prescription || 'No medications prescribed.';
    document.getElementById('prescription-modal').classList.add('open');
}

function closePrescriptionModal() {
    document.getElementById('prescription-modal').classList.remove('open');
}

// -------------------------------------------------------------
// Patient Profile
// -------------------------------------------------------------
async function initPatientProfile() {
    const form = document.getElementById('profile-form');
    if (!form) return;

    try {
        const res = await request('/patients/profile/me');
        if (res && res.data) {
            const p = res.data;
            if (p.dateOfBirth) document.getElementById('profile-dob').value = p.dateOfBirth;
            if (p.gender) document.getElementById('profile-gender').value = p.gender;
            if (p.bloodGroup) document.getElementById('profile-blood').value = p.bloodGroup;
            if (p.address) document.getElementById('profile-address').value = p.address;
            if (p.emergencyContact) document.getElementById('profile-emergency').value = p.emergencyContact;
            if (p.medicalHistory) document.getElementById('profile-medhistory').value = p.medicalHistory;
        }
    } catch (err) {
        console.warn('Could not load profile:', err);
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            dateOfBirth: document.getElementById('profile-dob').value,
            gender: document.getElementById('profile-gender').value,
            bloodGroup: document.getElementById('profile-blood').value,
            address: document.getElementById('profile-address').value.trim(),
            emergencyContact: document.getElementById('profile-emergency').value.trim(),
            medicalHistory: document.getElementById('profile-medhistory').value.trim()
        };

        try {
            const res = await request('/patients/profile/me', {
                method: 'PUT',
                body: JSON.stringify(payload)
            });
            if (res && res.success) {
                showToast('Profile updated successfully!', 'success');
            }
        } catch (err) {
            showToast(err.message || 'Failed to update profile', 'error');
        }
    });
}

// -------------------------------------------------------------
// Tab Switching
// -------------------------------------------------------------
function switchTab(tabId) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.style.display = 'none');

    const activeBtn = document.getElementById(`tab-btn-${tabId}`);
    const activeContent = document.getElementById(`tab-${tabId}`);

    if (activeBtn) activeBtn.classList.add('active');
    if (activeContent) activeContent.style.display = 'block';

    if (tabId === 'appointments') loadMyAppointments();
    if (tabId === 'doctors') loadDoctors();
}
