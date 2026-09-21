// Joblyn — UI interactions and dynamic state handling

document.addEventListener('DOMContentLoaded', function () {

    // --- Role Picker & Form Fields Sync ---
    const roleInputs = document.querySelectorAll('.role-pick input[type="radio"]');
    const employerOnly = document.getElementById('employerOnlyFields');

    function syncRolePickerState() {
        roleInputs.forEach(function (input) {
            const label = document.querySelector('label[for="' + input.id + '"]');
            if (input.checked) {
                if (label) label.classList.add('picked');
                // Check against upper or lowercase 'EMPLOYER'
                if (employerOnly) {
                    const isEmployer = input.value.toUpperCase() === 'EMPLOYER';
                    employerOnly.classList.toggle('d-none', !isEmployer);
                }
            } else {
                if (label) label.classList.remove('picked');
            }
        });
    }

    // Run on initial load to match pre-selected radio buttons (e.g. from ?role=employer query param)
    if (roleInputs.length > 0) {
        syncRolePickerState();

        roleInputs.forEach(function (input) {
            input.addEventListener('change', syncRolePickerState);
        });
    }

    // --- Admin Dashboard (Static Fallback UI) ---
    document.querySelectorAll('[data-action="approve"], [data-action="reject"]').forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const row = btn.closest('tr');
            if (!row) return;
            const badge = row.querySelector('.badge-status');
            if (!badge) return;
            if (btn.dataset.action === 'approve') {
                badge.className = 'badge-status badge-approved';
                badge.textContent = 'Published';
            } else {
                badge.className = 'badge-status badge-rejected';
                badge.textContent = 'Rejected';
            }
            const actions = row.querySelector('.row-actions');
            if (actions) actions.innerHTML = '<span class="text-muted small">Reviewed</span>';
        });
    });

    // --- Apply Modal Trigger Update ---
    const applyConfirm = document.getElementById('confirmApplyBtn');
    if (applyConfirm) {
        applyConfirm.addEventListener('click', function () {
            const applyTrigger = document.getElementById('applyTriggerBtn');
            if (applyTrigger) {
                applyTrigger.textContent = 'Application submitted';
                applyTrigger.classList.remove('btn-brass');
                applyTrigger.classList.add('btn-outline-ink');
                applyTrigger.setAttribute('disabled', 'true');
            }
        });
    }
});