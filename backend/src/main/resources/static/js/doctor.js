/**
 * Doctor Dashboard logic for Doctor Appointment Booking System
 */

let doctorAppointments = [];
let currentActionAppointmentId = null;

document.addEventListener('DOMContentLoaded', () => {
    checkDoctorAuth();
    loadDoctorAppointments();
    loadDoctorSlots();
    loadDoctorProfile();
    initCompleteAppointmentForm();
    initAddSlotForm();
    initDoctorProfileForm();
});

function checkDoctorAuth() {
    const user = getCurrentUser();
    if (!user || user.role !== 'ROLE_DOCTOR') {
        window.location.href = 'login.html';
        return;
    }
    const welcomeSpan = document.getElementById('doctor-welcome-name');
    if (welcomeSpan) {
        welcomeSpan.innerText = user.fullName || user.email;
    }
}

// -------------------------------------------------------------
// Appointments & Stats
// -------------------------------------------------------------
async function loadDoctorAppointments() {
    const tbody = document.getElementById('doctor-appointments-tbody');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: #64748b;">Loading appointments...</td></tr>';

    try {
        const res = await request('/appointments/my-appointments');
        if (!res || !res.data) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: #64748b;">No appointments found.</td></tr>';
            return;
        }

        doctorAppointments = res.data;
        updateStats(doctorAppointments);
        renderAppointmentsTable(doctorAppointments);
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: #dc2626;">Error: ${err.message}</td></tr>`;
    }
}

function updateStats(appointments) {
    const total = appointments.length;
    const pending = appointments.filter(a => a.status === 'PENDING').length;
    const confirmed = appointments.filter(a => a.status === 'CONFIRMED').length;
    const completed = appointments.filter(a => a.status === 'COMPLETED').length;

    const elTotal = document.getElementById('stat-total');
    const elPending = document.getElementById('stat-pending');
    const elConfirmed = document.getElementById('stat-confirmed');
    const elCompleted = document.getElementById('stat-completed');

    if (elTotal) elTotal.innerText = total;
    if (elPending) elPending.innerText = pending;
    if (elConfirmed) elConfirmed.innerText = confirmed;
    if (elCompleted) elCompleted.innerText = completed;
}

function filterAppointmentsByStatus(status) {
    if (!status || status === 'ALL') {
        renderAppointmentsTable(doctorAppointments);
    } else {
        const filtered = doctorAppointments.filter(a => a.status === status);
        renderAppointmentsTable(filtered);
    }
}

function renderAppointmentsTable(list) {
    const tbody = document.getElementById('doctor-appointments-tbody');
    if (!tbody) return;

    if (list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: #64748b;">No appointments in this category.</td></tr>';
        return;
    }

    tbody.innerHTML = list.map(apt => {
        const statusClass = `badge-${apt.status.toLowerCase()}`;
        return `
            <tr>
                <td><strong>#${apt.id}</strong></td>
                <td>
                    <strong>${apt.patientName || 'Patient'}</strong>
                    <div style="font-size: 0.8rem; color: #64748b;">${apt.patientPhone || apt.patientEmail || ''}</div>
                </td>
                <td>${apt.appointmentDate}</td>
                <td>${apt.startTime} - ${apt.endTime}</td>
                <td><span class="badge ${statusClass}">${apt.status}</span></td>
                <td style="max-width: 180px; font-size: 0.85rem; color: #475569;">${apt.reason || 'General'}</td>
                <td>
                    <div style="display: flex; gap: 0.35rem; flex-wrap: wrap;">
                        ${apt.status === 'PENDING' ? `
                            <button onclick="updateStatus(${apt.id}, 'CONFIRMED')" class="btn btn-success btn-sm">Accept</button>
                            <button onclick="updateStatus(${apt.id}, 'CANCELLED')" class="btn btn-danger btn-sm">Reject</button>
                        ` : ''}
                        ${apt.status === 'CONFIRMED' ? `
                            <button onclick="openCompleteModal(${apt.id}, '${(apt.patientName || '').replace(/'/g, "\\'")}')" class="btn btn-primary btn-sm">
                                Complete & Prescribe
                            </button>
                            <button onclick="updateStatus(${apt.id}, 'CANCELLED')" class="btn btn-danger btn-sm">Cancel</button>
                        ` : ''}
                        ${apt.status === 'COMPLETED' ? `
                            <button onclick="viewPrescriptionDetail('${encodeURIComponent(JSON.stringify(apt))}')" class="btn btn-secondary btn-sm">
                                View Rx
                            </button>
                        ` : ''}
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

async function updateStatus(appointmentId, status) {
    if (!confirm(`Are you sure you want to mark this appointment as ${status}?`)) return;

    try {
        const res = await request(`/appointments/${appointmentId}/status`, {
            method: 'PATCH',
            body: JSON.stringify({ status })
        });

        if (res && res.success) {
            showToast(`Appointment status changed to ${status}`, 'success');
            loadDoctorAppointments();
        }
    } catch (err) {
        showToast(err.message || 'Failed to update status', 'error');
    }
}

// -------------------------------------------------------------
// Complete Appointment & Prescription Modal
// -------------------------------------------------------------
function openCompleteModal(appointmentId, patientName) {
    currentActionAppointmentId = appointmentId;
    document.getElementById('complete-modal-patient-name').innerText = patientName;
    document.getElementById('complete-notes').value = '';
    document.getElementById('complete-prescription').value = '';
    document.getElementById('complete-modal').classList.add('open');
}

function closeCompleteModal() {
    document.getElementById('complete-modal').classList.remove('open');
    currentActionAppointmentId = null;
}

function initCompleteAppointmentForm() {
    const form = document.getElementById('complete-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!currentActionAppointmentId) return;

        const doctorNotes = document.getElementById('complete-notes').value.trim();
        const prescription = document.getElementById('complete-prescription').value.trim();
        const submitBtn = document.getElementById('complete-submit-btn');

        submitBtn.disabled = true;
        submitBtn.innerText = 'Saving...';

        try {
            const res = await request(`/appointments/${currentActionAppointmentId}/status`, {
                method: 'PATCH',
                body: JSON.stringify({
                    status: 'COMPLETED',
                    doctorNotes,
                    prescription
                })
            });

            if (res && res.success) {
                showToast('Appointment completed and prescription issued!', 'success');
                closeCompleteModal();
                loadDoctorAppointments();
            }
        } catch (err) {
            showToast(err.message || 'Failed to complete appointment', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerText = 'Complete & Save';
        }
    });
}

function viewPrescriptionDetail(encodedApt) {
    const apt = JSON.parse(decodeURIComponent(encodedApt));
    alert(`PRESCRIPTION & CLINICAL NOTES\n\nPatient: ${apt.patientName}\nDate: ${apt.appointmentDate} (${apt.startTime})\n\nClinical Notes:\n${apt.doctorNotes || 'None'}\n\nPrescription:\n${apt.prescription || 'None'}`);
}

// -------------------------------------------------------------
// Doctor Availability Slots Management
// -------------------------------------------------------------
async function loadDoctorSlots() {
    const container = document.getElementById('doctor-slots-list');
    if (!container) return;

    container.innerHTML = '<div style="color: #64748b;">Loading your upcoming slots...</div>';

    try {
        const res = await request('/doctors/slots/my-slots');
        if (!res || !res.data || res.data.length === 0) {
            container.innerHTML = '<div style="color: #64748b;">No custom slots configured yet. Default slots are automatically generated when patients select a date.</div>';
            return;
        }

        container.innerHTML = `
            <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 0.75rem;">
                ${res.data.map(slot => `
                    <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 0.75rem; display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <div style="font-weight: 700; font-size: 0.9rem;">${slot.slotDate}</div>
                            <div style="color: #64748b; font-size: 0.85rem;">${slot.startTime} - ${slot.endTime}</div>
                            <span class="badge ${slot.booked ? 'badge-cancelled' : 'badge-confirmed'}" style="margin-top: 0.3rem;">
                                ${slot.booked ? 'Booked' : 'Available'}
                            </span>
                        </div>
                        ${!slot.booked ? `
                            <button onclick="deleteSlot(${slot.id})" class="btn btn-secondary btn-sm" style="color: #b91c1c;" title="Delete slot">✕</button>
                        ` : ''}
                    </div>
                `).join('')}
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div style="color: #dc2626;">Error loading slots: ${err.message}</div>`;
    }
}

function initAddSlotForm() {
    const form = document.getElementById('add-slot-form');
    if (!form) return;

    const dateInput = document.getElementById('slot-date');
    if (dateInput) {
        dateInput.value = new Date().toISOString().split('T')[0];
        dateInput.min = new Date().toISOString().split('T')[0];
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const slotDate = document.getElementById('slot-date').value;
        const startTime = document.getElementById('slot-start-time').value;
        const endTime = document.getElementById('slot-end-time').value;

        if (!slotDate || !startTime || !endTime) {
            showToast('Please fill all slot fields', 'error');
            return;
        }

        try {
            const res = await request('/doctors/slots', {
                method: 'POST',
                body: JSON.stringify({ slotDate, startTime, endTime })
            });

            if (res && res.success) {
                showToast('Availability slot added successfully!', 'success');
                loadDoctorSlots();
            }
        } catch (err) {
            showToast(err.message || 'Failed to add slot', 'error');
        }
    });
}

async function deleteSlot(slotId) {
    if (!confirm('Are you sure you want to remove this availability slot?')) return;

    try {
        const res = await request(`/doctors/slots/${slotId}`, { method: 'DELETE' });
        if (res && res.success) {
            showToast('Slot removed', 'success');
            loadDoctorSlots();
        }
    } catch (err) {
        showToast(err.message || 'Failed to delete slot', 'error');
    }
}

// -------------------------------------------------------------
// Doctor Profile Settings
// -------------------------------------------------------------
async function loadDoctorProfile() {
    try {
        const res = await request('/doctors/profile/me');
        if (res && res.data) {
            const d = res.data;
            if (document.getElementById('doc-name')) document.getElementById('doc-name').value = d.fullName || '';
            if (document.getElementById('doc-phone')) document.getElementById('doc-phone').value = d.phone || '';
            if (document.getElementById('doc-spec')) document.getElementById('doc-spec').value = d.specialization || '';
            if (document.getElementById('doc-qual')) document.getElementById('doc-qual').value = d.qualification || '';
            if (document.getElementById('doc-exp')) document.getElementById('doc-exp').value = d.experienceYears || '';
            if (document.getElementById('doc-fee')) document.getElementById('doc-fee').value = d.consultationFee || '';
            if (document.getElementById('doc-clinic')) document.getElementById('doc-clinic').value = d.clinicAddress || '';
            if (document.getElementById('doc-bio')) document.getElementById('doc-bio').value = d.bio || '';
            if (document.getElementById('doc-photo')) document.getElementById('doc-photo').value = d.profileImageUrl || '';
        }
    } catch (err) {
        console.warn('Could not load doctor profile:', err);
    }
}

function initDoctorProfileForm() {
    const form = document.getElementById('doctor-profile-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const payload = {
            fullName: document.getElementById('doc-name').value.trim(),
            phone: document.getElementById('doc-phone').value.trim(),
            specialization: document.getElementById('doc-spec').value.trim(),
            qualification: document.getElementById('doc-qual').value.trim(),
            experienceYears: parseInt(document.getElementById('doc-exp').value, 10),
            consultationFee: parseFloat(document.getElementById('doc-fee').value),
            clinicAddress: document.getElementById('doc-clinic').value.trim(),
            bio: document.getElementById('doc-bio').value.trim(),
            profileImageUrl: document.getElementById('doc-photo').value.trim()
        };

        try {
            const res = await request('/doctors/profile/me', {
                method: 'PUT',
                body: JSON.stringify(payload)
            });

            if (res && res.success) {
                showToast('Doctor profile updated successfully!', 'success');
                // Update current cached user
                const user = getCurrentUser();
                if (user) {
                    user.fullName = payload.fullName;
                    localStorage.setItem('doctorapp_user', JSON.stringify(user));
                    setupNavbar();
                }
            }
        } catch (err) {
            showToast(err.message || 'Failed to update profile', 'error');
        }
    });
}

// -------------------------------------------------------------
// Tab Switching
// -------------------------------------------------------------
function switchDoctorTab(tabId) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.style.display = 'none');

    const activeBtn = document.getElementById(`tab-btn-${tabId}`);
    const activeContent = document.getElementById(`tab-${tabId}`);

    if (activeBtn) activeBtn.classList.add('active');
    if (activeContent) activeContent.style.display = 'block';

    if (tabId === 'appointments') loadDoctorAppointments();
    if (tabId === 'slots') loadDoctorSlots();
    if (tabId === 'profile') loadDoctorProfile();
}
