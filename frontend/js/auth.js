/**
 * Authentication handling for Doctor Appointment Booking System
 */

document.addEventListener('DOMContentLoaded', () => {
    initLoginForm();
    initRegisterForm();
});

function initLoginForm() {
    const loginForm = document.getElementById('login-form');
    if (!loginForm) return;

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const email = document.getElementById('login-email').value.trim();
        const password = document.getElementById('login-password').value;
        const submitBtn = document.getElementById('login-submit-btn');

        if (!email || !password) {
            showToast('Please enter both email and password.', 'error');
            return;
        }

        submitBtn.disabled = true;
        submitBtn.innerText = 'Signing in...';

        try {
            const res = await request('/auth/login', {
                method: 'POST',
                body: JSON.stringify({ email, password })
            });

            if (res && res.success) {
                setSession(res.data);
                showToast('Login successful! Redirecting...', 'success');
                setTimeout(() => {
                    if (res.data.role === 'ROLE_DOCTOR') {
                        window.location.href = 'doctor-dashboard.html';
                    } else {
                        window.location.href = 'patient-dashboard.html';
                    }
                }, 800);
            }
        } catch (err) {
            showToast(err.message || 'Login failed. Please check credentials.', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerText = 'Sign In';
        }
    });
}

function fillDemoLogin(role) {
    const emailField = document.getElementById('login-email');
    const passwordField = document.getElementById('login-password');
    if (!emailField || !passwordField) return;

    if (role === 'doctor') {
        emailField.value = 'doctor@demo.com';
        passwordField.value = 'password123';
    } else {
        emailField.value = 'patient@demo.com';
        passwordField.value = 'password123';
    }
    showToast(`Filled demo credentials for ${role}! Click 'Sign In' or Submit.`, 'info');
}

let selectedRole = 'ROLE_PATIENT';

function setRegisterRole(role) {
    selectedRole = role;
    const patientTab = document.getElementById('role-btn-patient');
    const doctorTab = document.getElementById('role-btn-doctor');
    const doctorFields = document.getElementById('doctor-specific-fields');
    const patientFields = document.getElementById('patient-specific-fields');

    if (role === 'ROLE_DOCTOR') {
        doctorTab.classList.add('active');
        patientTab.classList.remove('active');
        if (doctorFields) doctorFields.style.display = 'block';
        if (patientFields) patientFields.style.display = 'none';
    } else {
        patientTab.classList.add('active');
        doctorTab.classList.remove('active');
        if (doctorFields) doctorFields.style.display = 'none';
        if (patientFields) patientFields.style.display = 'block';
    }
}

function initRegisterForm() {
    const regForm = document.getElementById('register-form');
    if (!regForm) return;

    regForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const fullName = document.getElementById('reg-name').value.trim();
        const email = document.getElementById('reg-email').value.trim();
        const password = document.getElementById('reg-password').value;
        const phone = document.getElementById('reg-phone').value.trim();
        const submitBtn = document.getElementById('reg-submit-btn');

        if (!fullName || !email || !password) {
            showToast('Please fill in all required fields.', 'error');
            return;
        }

        const payload = {
            fullName,
            email,
            password,
            phone,
            role: selectedRole
        };

        if (selectedRole === 'ROLE_DOCTOR') {
            payload.specialization = document.getElementById('reg-spec').value.trim() || 'General Physician';
            payload.qualification = document.getElementById('reg-qual').value.trim() || 'MBBS';
            payload.experienceYears = parseInt(document.getElementById('reg-exp').value, 10) || 1;
            payload.consultationFee = parseFloat(document.getElementById('reg-fee').value) || 50.0;
            payload.clinicAddress = document.getElementById('reg-clinic').value.trim() || 'City Health Center';
            payload.bio = document.getElementById('reg-bio').value.trim() || '';
            payload.profileImageUrl = document.getElementById('reg-photo').value.trim() || '';
        } else {
            payload.dateOfBirth = document.getElementById('reg-dob').value;
            payload.gender = document.getElementById('reg-gender').value;
            payload.bloodGroup = document.getElementById('reg-blood').value;
            payload.address = document.getElementById('reg-address').value.trim();
            payload.emergencyContact = document.getElementById('reg-emergency').value.trim();
            payload.medicalHistory = document.getElementById('reg-medhistory').value.trim();
        }

        submitBtn.disabled = true;
        submitBtn.innerText = 'Creating account...';

        try {
            const res = await request('/auth/register', {
                method: 'POST',
                body: JSON.stringify(payload)
            });

            if (res && res.success) {
                setSession(res.data);
                showToast('Registration successful! Redirecting...', 'success');
                setTimeout(() => {
                    if (selectedRole === 'ROLE_DOCTOR') {
                        window.location.href = 'doctor-dashboard.html';
                    } else {
                        window.location.href = 'patient-dashboard.html';
                    }
                }, 800);
            }
        } catch (err) {
            showToast(err.message || 'Registration failed.', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerText = 'Create Account';
        }
    });
}
