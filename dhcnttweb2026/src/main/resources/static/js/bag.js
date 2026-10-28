/**
 * Client interactions for Bag (Túi hàng)
 * Real-time stepper calculation, dynamic remove/add, empty state switching, and sync with Database API.
 */
document.addEventListener('DOMContentLoaded', function () {
  const bagItemsContainer = document.getElementById('bagItemsContainer');
  const bagActiveView = document.getElementById('bagActiveView');
  const bagEmptyView = document.getElementById('bagEmptyView');
  const subtotalEl = document.getElementById('bagSubtotal');
  const totalEl = document.getElementById('bagTotal');
  const navBagBadge = document.getElementById('globalNavBagBadge') || document.querySelector('.apple-global-nav .bi-bag + .badge');
  const promoAccordionBtn = document.getElementById('promoAccordionBtn');
  const promoInputCollapse = document.getElementById('promoInputCollapse');
  const promoApplyBtn = document.getElementById('promoApplyBtn');
  const promoInput = document.getElementById('promoInput');
  const discountRow = document.getElementById('discountRow');
  const discountValEl = document.getElementById('discountVal');

  let discountAmount = 0;

  function formatVND(amount) {
    return new Intl.NumberFormat('de-DE').format(amount) + 'đ';
  }

  // Cập nhật lại tổng tiền và số lượng trên badge
  function recalculateTotals() {
    let subtotal = 0;
    let totalItems = 0;

    const rows = document.querySelectorAll('.bag-item-row');
    rows.forEach(row => {
      const unitPrice = parseInt(row.getAttribute('data-unit-price'), 10) || 0;
      const qtyEl = row.querySelector('.bag-stepper-val');
      const qty = parseInt(qtyEl.textContent, 10) || 1;
      const lineTotal = unitPrice * qty;

      subtotal += lineTotal;
      totalItems += qty;

      const lineTotalEl = row.querySelector('.bag-item-line-total');
      if (lineTotalEl) {
        lineTotalEl.textContent = formatVND(lineTotal);
      }

      // Khống chế trạng thái nút stepper
      const btnMinus = row.querySelector('.btn-qty-minus');
      const btnPlus = row.querySelector('.btn-qty-plus');
      const stock = parseInt(row.getAttribute('data-stock'), 10) || 99;

      if (btnMinus) {
        btnMinus.title = qty <= 1 ? "Xóa khỏi túi hàng" : "Giảm số lượng";
      }
      if (btnPlus) {
        btnPlus.disabled = qty >= stock;
      }
    });

    if (subtotalEl) subtotalEl.textContent = formatVND(subtotal);

    const finalTotal = Math.max(0, subtotal - discountAmount);
    if (totalEl) totalEl.textContent = formatVND(finalTotal);

    // Cập nhật badge trên thanh Global Nav
    if (navBagBadge) {
      navBagBadge.textContent = totalItems;
      navBagBadge.style.display = totalItems > 0 ? 'inline-block' : 'none';
    }

    // Kiểm tra chuyển sang trạng thái Empty State
    if (rows.length === 0) {
      if (bagActiveView) bagActiveView.classList.add('d-none');
      if (bagEmptyView) bagEmptyView.classList.remove('d-none');
    } else {
      if (bagActiveView) bagActiveView.classList.remove('d-none');
      if (bagEmptyView) bagEmptyView.classList.add('d-none');
    }
  }

  // Gắn sự kiện cho từng dòng Bag Item (Gọi Backend API để lưu DB)
  function attachRowEvents(row) {
    const btnMinus = row.querySelector('.btn-qty-minus');
    const btnPlus = row.querySelector('.btn-qty-plus');
    const btnRemove = row.querySelector('.bag-link-remove');
    const qtyValEl = row.querySelector('.bag-stepper-val');
    const stock = parseInt(row.getAttribute('data-stock'), 10) || 99;
    const cartItemId = row.getAttribute('data-id');

    if (btnMinus) {
      btnMinus.addEventListener('click', function () {
        let currentQty = parseInt(qtyValEl.textContent, 10) || 1;
        if (currentQty > 1) {
          btnMinus.disabled = true;
          fetch('/api/bag/update', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ cartItemId: cartItemId, quantity: currentQty - 1 })
          })
          .then(res => res.json())
          .then(data => {
            btnMinus.disabled = false;
            if (data.success) {
              qtyValEl.textContent = currentQty - 1;
              recalculateTotals();
            } else {
              alert(data.message || 'Không thể cập nhật số lượng.');
            }
          })
          .catch(err => {
            btnMinus.disabled = false;
            console.error(err);
          });
        } else {
          // Khi giảm về 0: xác nhận xóa
          removeRowWithApi(row, cartItemId);
        }
      });
    }

    if (btnPlus) {
      btnPlus.addEventListener('click', function () {
        let currentQty = parseInt(qtyValEl.textContent, 10) || 1;
        if (currentQty < stock) {
          btnPlus.disabled = true;
          fetch('/api/bag/update', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ cartItemId: cartItemId, quantity: currentQty + 1 })
          })
          .then(res => res.json())
          .then(data => {
            btnPlus.disabled = false;
            if (data.success) {
              qtyValEl.textContent = currentQty + 1;
              recalculateTotals();
            } else {
              alert(data.message || 'Không thể tăng số lượng.');
            }
          })
          .catch(err => {
            btnPlus.disabled = false;
            console.error(err);
          });
        }
      });
    }

    if (btnRemove) {
      btnRemove.addEventListener('click', function (e) {
        e.preventDefault();
        removeRowWithApi(row, cartItemId);
      });
    }
  }

  function removeRowWithApi(row, cartItemId) {
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này khỏi túi hàng?')) {
      return;
    }
    fetch('/api/bag/remove', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ cartItemId: cartItemId })
    })
    .then(res => res.json())
    .then(data => {
      if (data.success) {
        row.classList.add('removing');
        setTimeout(() => {
          row.remove();
          recalculateTotals();
          showToast("Đã xóa sản phẩm khỏi túi hàng.");
        }, 300);
      } else {
        alert(data.message || 'Không thể xóa sản phẩm.');
      }
    })
    .catch(err => console.error(err));
  }

  // Khởi tạo sự kiện cho các item ban đầu
  document.querySelectorAll('.bag-item-row').forEach(attachRowEvents);

  // Toggle mã ưu đãi
  if (promoAccordionBtn && promoInputCollapse) {
    promoAccordionBtn.addEventListener('click', function () {
      const isHidden = promoInputCollapse.classList.contains('d-none');
      if (isHidden) {
        promoInputCollapse.classList.remove('d-none');
      } else {
        promoInputCollapse.classList.add('d-none');
      }
    });
  }

  // Áp dụng mã ưu đãi (mock)
  if (promoApplyBtn && promoInput) {
    promoApplyBtn.addEventListener('click', function () {
      const code = promoInput.value.trim().toUpperCase();
      if (!code) return;

      if (code === 'APPLEVIP' || code === 'DHSTORE') {
        discountAmount = 500000;
        if (discountRow) discountRow.classList.remove('d-none');
        if (discountValEl) discountValEl.textContent = '-' + formatVND(discountAmount);
        recalculateTotals();
        showToast("Áp dụng mã ưu đãi thành công (-500.000đ)");
      } else {
        alert("Mã ưu đãi không hợp lệ. Hãy thử dùng mã: APPLEVIP");
      }
    });
  }

  // Sự kiện Quick Add từ khối Phụ kiện tương thích
  const quickAddBtns = document.querySelectorAll('.btn-quick-add');
  quickAddBtns.forEach(btn => {
    btn.addEventListener('click', function () {
      const sku = this.getAttribute('data-sku');
      const name = this.getAttribute('data-name');
      const originalText = this.innerHTML;

      this.disabled = true;
      this.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Đang thêm...';

      fetch('/api/bag/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sku: sku, quantity: 1 })
      })
      .then(async res => {
        const data = await res.json();
        if (res.status === 401 || (data && data.requireLogin)) {
          window.location.href = data.redirectUrl || ('/login?returnUrl=' + encodeURIComponent(window.location.pathname));
          return null;
        }
        return data;
      })
      .then(data => {
        if (!data) return;
        this.disabled = false;
        this.innerHTML = originalText;
        if (data.success) {
          showToast(`Đã thêm ${name} vào túi hàng!`);
          setTimeout(() => {
            window.location.reload();
          }, 800);
        } else {
          alert(data.message || 'Không thể thêm phụ kiện vào túi hàng.');
        }
      })
      .catch(err => {
        this.disabled = false;
        this.innerHTML = originalText;
        console.error(err);
      });
    });
  });

  // Toast Notification helper
  function showToast(message) {
    let toast = document.getElementById('bagActionToast');
    if (!toast) {
      toast = document.createElement('div');
      toast.id = 'bagActionToast';
      toast.style.cssText = `
        position: fixed;
        bottom: 24px;
        right: 24px;
        background-color: #1d1d1f;
        color: #ffffff;
        padding: 12px 24px;
        border-radius: 9999px;
        font-size: 14px;
        z-index: 9999;
        box-shadow: 0 4px 20px rgba(0,0,0,0.25);
        display: flex;
        align-items: center;
        gap: 8px;
        transition: opacity 0.3s ease, transform 0.3s ease;
      `;
      document.body.appendChild(toast);
    }
    toast.innerHTML = `<i class="bi bi-check-circle-fill text-primary"></i> ${message}`;
    toast.style.opacity = '1';
    toast.style.transform = 'translateY(0)';

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
    }, 3000);
  }

  // Khởi chạy tính toán ban đầu
  recalculateTotals();
});
