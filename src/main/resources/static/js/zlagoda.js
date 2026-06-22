/* ============================================================
   Zlagoda — confirm dialog helper
   Replaces native confirm() with a themed Bootstrap modal.

   Any element can opt in:
     data-zl-confirm="Ви впевнені?"            (body message, required)
     data-zl-confirm-title="Видалити товар"     (heading, optional)
     data-zl-confirm-ok="Видалити"             (confirm button label, optional)

   Works for <a href> (navigates) and <button> inside a <form> (submits).
   ============================================================ */
(function () {
  'use strict';

  let pendingEl = null;

  function setup() {
    const modalEl = document.getElementById('zlConfirmModal');
    if (!modalEl || typeof bootstrap === 'undefined') return;

    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
    const titleEl = modalEl.querySelector('#zlConfirmTitle');
    const bodyEl = modalEl.querySelector('#zlConfirmBody');
    const okBtn = modalEl.querySelector('#zlConfirmOk');

    // Intercept clicks on anything marked with data-zl-confirm
    document.addEventListener('click', function (e) {
      const trigger = e.target.closest('[data-zl-confirm]');
      if (!trigger) return;
      e.preventDefault();
      pendingEl = trigger;
      if (titleEl) titleEl.textContent = trigger.getAttribute('data-zl-confirm-title') || 'Підтвердити дію';
      if (bodyEl) bodyEl.textContent = trigger.getAttribute('data-zl-confirm') || 'Ви впевнені?';
      if (okBtn) okBtn.textContent = trigger.getAttribute('data-zl-confirm-ok') || 'Підтвердити';
      modal.show();
    });

    // Run the pending action when the confirm button is pressed
    okBtn.addEventListener('click', function () {
      if (!pendingEl) return;
      modal.hide();
      const el = pendingEl;
      pendingEl = null;
      if (el.tagName === 'A' && el.getAttribute('href')) {
        window.location.href = el.getAttribute('href');
      } else {
        const form = el.closest('form');
        if (form) form.submit();
      }
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', setup);
  } else {
    setup();
  }
})();
