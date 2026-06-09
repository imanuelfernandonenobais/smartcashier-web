document.addEventListener('DOMContentLoaded', () => {
  const flashMessages = Array.from(document.querySelectorAll('.js-flash-message'));

  if (flashMessages.length > 0) {
    if (typeof Swal === 'undefined') {
      flashMessages.forEach((element) => element.classList.remove('d-none'));
    } else {
      const toast = Swal.mixin({
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 3000,
        timerProgressBar: true,
        didOpen: (popup) => {
          popup.addEventListener('mouseenter', Swal.stopTimer);
          popup.addEventListener('mouseleave', Swal.resumeTimer);
        }
      });

      (async () => {
        for (const element of flashMessages) {
          const message = element.textContent ? element.textContent.trim() : '';
          const type = element.dataset.alertType || 'info';

          if (!message) {
            continue;
          }

          if (type === 'success') {
            await toast.fire({
              icon: 'success',
              title: message
            });
            continue;
          }

          await Swal.fire({
            icon: type === 'error' ? 'error' : 'info',
            title: type === 'error' ? 'Terjadi kesalahan' : 'Informasi',
            text: message,
            confirmButtonText: 'Tutup',
            customClass: {
              confirmButton: 'btn btn-primary'
            },
            buttonsStyling: false
          });
        }
      })();
    }
  }

  const confirmableForms = Array.from(document.querySelectorAll('form.js-confirm-submit'));

  confirmableForms.forEach((form) => {
    form.addEventListener('submit', (event) => {
      if (form.dataset.confirmed === 'true') {
        return;
      }

      const title = form.dataset.confirmTitle || 'Lanjutkan tindakan ini?';
      const text = form.dataset.confirmText || 'Perubahan ini tidak dapat dibatalkan.';
      const confirmText = form.dataset.confirmConfirmText || 'Ya, lanjutkan';
      const cancelText = form.dataset.confirmCancelText || 'Batal';

      event.preventDefault();

      if (typeof Swal === 'undefined') {
        if (window.confirm(`${title}\n\n${text}`)) {
          form.dataset.confirmed = 'true';
          HTMLFormElement.prototype.submit.call(form);
        }
        return;
      }

      Swal.fire({
        title,
        text,
        icon: form.dataset.confirmIcon || 'warning',
        showCancelButton: true,
        confirmButtonText: confirmText,
        cancelButtonText: cancelText,
        reverseButtons: true,
        focusCancel: true,
        customClass: {
          confirmButton: 'btn btn-danger',
          cancelButton: 'btn btn-light me-2'
        },
        buttonsStyling: false
      }).then((result) => {
        if (!result.isConfirmed) {
          return;
        }

        form.dataset.confirmed = 'true';
        HTMLFormElement.prototype.submit.call(form);
      });
    });
  });
});
