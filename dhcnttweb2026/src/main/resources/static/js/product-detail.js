/**
 * Product Detail Configurator & Dynamic State Switching
 * Handles variant selection (Color, Storage), live price updates, gallery sync, and Add to Bag notification.
 */

document.addEventListener('DOMContentLoaded', function () {
  let variants = window.PRODUCT_VARIANTS_DATA || [];
  if (!variants || variants.length === 0) {
    const variantsDataElem = document.getElementById('productVariantsJson');
    if (variantsDataElem) {
      try {
        variants = JSON.parse(variantsDataElem.textContent);
      } catch (err) {
        console.error('Failed to parse variants JSON', err);
      }
    }
  }
  if (!variants || variants.length === 0) return;

  const productName = window.PRODUCT_NAME || 'Sản phẩm Apple';

  // Initial State: Default to first available variant
  const firstVariant = variants[0];
  let currentColor = firstVariant.colorName || '';
  let currentStorage = firstVariant.storageCapacity || '';
  let currentImageIndex = 0;

  // DOM Elements
  const mainImage = document.getElementById('galleryMainImg');
  const thumbsContainer = document.getElementById('galleryThumbsContainer');
  const selectedColorLabel = document.getElementById('selectedColorLabel');
  const configSummaryName = document.getElementById('configSummaryName');
  const configSummaryPrice = document.getElementById('configSummaryPrice');
  const subnavPrice = document.getElementById('subnavPrice');
  const subnavBuyBtn = document.getElementById('subnavBuyBtn');
  const addToBagBtn = document.getElementById('addToBagBtn');

  // Bag Toast Elements
  const bagToast = document.getElementById('appleBagToast');
  const toastImg = document.getElementById('toastImg');
  const toastVariantDesc = document.getElementById('toastVariantDesc');
  const toastPrice = document.getElementById('toastPrice');
  const toastCloseBtn = document.getElementById('toastCloseBtn');
  const bagBadge = document.getElementById('globalNavBagBadge') || document.querySelector('.apple-global-nav .badge');

  let toastTimeout = null;

  function findCurrentVariant() {
    return variants.find(v => v.colorName === currentColor && v.storageCapacity === currentStorage) || variants[0];
  }

  function updateDisplay() {
    const variant = findCurrentVariant();
    if (!variant) return;

    // 1. Update text & labels
    if (selectedColorLabel) {
      selectedColorLabel.textContent = currentColor;
    }
    if (configSummaryName) {
      const storagePart = (variant.storageCapacity && variant.storageCapacity !== 'Tiêu chuẩn') ? ` ${variant.storageCapacity}` : '';
      const colorPart = (variant.colorName && variant.colorName !== 'Tiêu chuẩn') ? ` ${variant.colorName}` : '';
      configSummaryName.textContent = `${productName}${storagePart}${colorPart}`;
    }
    if (configSummaryPrice) {
      configSummaryPrice.textContent = variant.formattedPrice;
    }
    if (subnavPrice) {
      subnavPrice.textContent = variant.formattedPrice;
    }

    // 2. Update Gallery Images
    if (variant.images && variant.images.length > 0) {
      const targetImgSrc = variant.images[currentImageIndex] || variant.images[0];
      if (mainImage && mainImage.src !== targetImgSrc) {
        mainImage.style.opacity = '0.3';
        mainImage.style.transform = 'scale(0.98)';
        setTimeout(() => {
          mainImage.src = targetImgSrc;
          mainImage.style.opacity = '1';
          mainImage.style.transform = 'scale(1)';
        }, 120);
      }

      // Re-render thumbnails if needed
      if (thumbsContainer) {
        thumbsContainer.innerHTML = '';
        variant.images.forEach((imgUrl, index) => {
          const thumbBtn = document.createElement('button');
          thumbBtn.className = `gallery-thumb-btn ${index === currentImageIndex ? 'active' : ''}`;
          thumbBtn.type = 'button';
          thumbBtn.innerHTML = `<img src="${imgUrl}" alt="Góc chụp ${index + 1}">`;
          thumbBtn.addEventListener('click', function () {
            currentImageIndex = index;
            updateDisplay();
          });
          thumbsContainer.appendChild(thumbBtn);
        });
      }
    }

    // 3. Stock handling
    if (variant.stockQuantity <= 0) {
      if (addToBagBtn) {
        addToBagBtn.disabled = true;
        addToBagBtn.textContent = 'Hết hàng tạm thời';
      }
      if (subnavBuyBtn) {
        subnavBuyBtn.disabled = true;
        subnavBuyBtn.textContent = 'Hết hàng';
      }
    } else {
      if (addToBagBtn) {
        addToBagBtn.disabled = false;
        addToBagBtn.innerHTML = '<i class="bi bi-bag-plus me-2"></i> Thêm vào túi hàng';
      }
      if (subnavBuyBtn) {
        subnavBuyBtn.disabled = false;
        subnavBuyBtn.textContent = 'Mua ngay';
      }
    }
  }

  // 1. Event Listeners for Color Swatches
  const colorSwatches = document.querySelectorAll('.color-swatch-item');
  colorSwatches.forEach(swatch => {
    swatch.addEventListener('click', function () {
      colorSwatches.forEach(s => s.classList.remove('active'));
      this.classList.add('active');
      currentColor = this.getAttribute('data-color-name');
      currentImageIndex = 0; // Reset thumbnail to primary angle
      updateDisplay();
    });
  });

  // 2. Event Listeners for Storage Option Cards
  const storageCards = document.querySelectorAll('.storage-card');
  storageCards.forEach(card => {
    card.addEventListener('click', function () {
      storageCards.forEach(c => c.classList.remove('active'));
      this.classList.add('active');
      currentStorage = this.getAttribute('data-storage-capacity');
      updateDisplay();
    });
  });

  // 3. Add To Bag Trigger via AJAX
  function triggerAddToBag() {
    const variant = findCurrentVariant();
    if (!variant || variant.stockQuantity <= 0) return;

    if (addToBagBtn) {
      addToBagBtn.disabled = true;
    }

    fetch('/api/bag/add', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        variantId: variant.id,
        sku: variant.sku,
        quantity: 1
      })
    })
    .then(async response => {
      const data = await response.json();
      if (response.status === 401 || (data && data.requireLogin)) {
        window.location.href = data.redirectUrl || ('/login?returnUrl=' + encodeURIComponent(window.location.pathname));
        return null;
      }
      return data;
    })
    .then(data => {
      if (!data) return;
      if (addToBagBtn) {
        addToBagBtn.disabled = false;
      }

      if (data.success) {
        // Cập nhật badge trên Global Nav
        if (bagBadge) {
          bagBadge.textContent = data.totalItems;
          bagBadge.style.display = data.totalItems > 0 ? 'inline-block' : 'none';
          bagBadge.classList.add('animate__animated', 'animate__pulse');
        }

        // Điền thông tin và mở Apple Toast
        if (bagToast) {
          if (toastImg) toastImg.src = (variant.images && variant.images[0]) || '';
          if (toastVariantDesc) {
            const storagePart = (variant.storageCapacity && variant.storageCapacity !== 'Tiêu chuẩn') ? ` ${variant.storageCapacity}` : '';
            const colorPart = (variant.colorName && variant.colorName !== 'Tiêu chuẩn') ? ` - ${variant.colorName}` : '';
            toastVariantDesc.textContent = `${productName}${storagePart}${colorPart}`;
          }
          if (toastPrice) toastPrice.textContent = variant.formattedPrice;

          bagToast.classList.add('show');

          if (toastTimeout) clearTimeout(toastTimeout);
          toastTimeout = setTimeout(() => {
            bagToast.classList.remove('show');
          }, 5000);
        }
      } else {
        alert(data.message || 'Không thể thêm sản phẩm vào túi hàng.');
      }
    })
    .catch(err => {
      if (addToBagBtn) {
        addToBagBtn.disabled = false;
      }
      console.error('Lỗi khi gửi yêu cầu thêm vào túi hàng:', err);
    });
  }

  if (addToBagBtn) {
    addToBagBtn.addEventListener('click', triggerAddToBag);
  }

  if (subnavBuyBtn) {
    subnavBuyBtn.addEventListener('click', function () {
      triggerAddToBag();
    });
  }

  if (toastCloseBtn) {
    toastCloseBtn.addEventListener('click', function () {
      if (bagToast) bagToast.classList.remove('show');
      if (toastTimeout) clearTimeout(toastTimeout);
    });
  }

  // Initial render
  updateDisplay();
});
