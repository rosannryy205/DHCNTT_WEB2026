/**
 * Apple Design System - Products Catalog & AJAX Filtering Engine
 * DH Store (2026)
 */

document.addEventListener('DOMContentLoaded', function () {
  'use strict';

  // State
  let currentPage = 1;
  let priceDebounceTimer = null;

  // DOM Elements
  const gridContainer = document.getElementById('product-grid-container');
  const sortSelect = document.getElementById('catalogSortSelect');
  const minPriceInput = document.getElementById('minPriceInput');
  const maxPriceInput = document.getElementById('maxPriceInput');
  const quickPricePills = document.querySelectorAll('.quick-price-pill');
  const desktopResetBtn = document.getElementById('desktopResetFiltersBtn');
  const storageFilterGroup = document.getElementById('storageFilterGroup');
  const mobileFilterContent = document.getElementById('mobileFilterContent');
  const mobileResetBtn = document.getElementById('mobileResetFiltersBtn');

  // Initialize
  initMobileFilterSync();
  bindFilterEventListeners();
  bindFragmentEventListeners();
  updateContextualFacets();

  // Listen to browser Back/Forward navigation
  window.addEventListener('popstate', function () {
    syncInputsFromUrl();
    fetchFilteredProducts(false);
  });

  /**
   * Bind event listeners for sidebar filter inputs
   */
  function bindFilterEventListeners() {
    // Checkbox changes (Category, Storage, Color)
    document.addEventListener('change', function (e) {
      if (
        e.target.matches('.filter-category-checkbox') ||
        e.target.matches('.filter-storage-checkbox') ||
        e.target.matches('.filter-color-checkbox')
      ) {
        currentPage = 1;
        updateContextualFacets();
        fetchFilteredProducts(true);
      }
    });

    // Quick price pills
    quickPricePills.forEach(function (pill) {
      pill.addEventListener('click', function () {
        const minVal = pill.getAttribute('data-min');
        const maxVal = pill.getAttribute('data-max');

        quickPricePills.forEach(p => p.classList.remove('active'));
        pill.classList.add('active');

        if (minPriceInput) minPriceInput.value = minVal !== 'null' && minVal ? minVal : '';
        if (maxPriceInput) maxPriceInput.value = maxVal !== 'null' && maxVal ? maxVal : '';

        currentPage = 1;
        fetchFilteredProducts(true);
      });
    });

    // Price input typing with debounce
    if (minPriceInput && maxPriceInput) {
      const handlePriceInput = function () {
        quickPricePills.forEach(p => p.classList.remove('active'));
        clearTimeout(priceDebounceTimer);
        priceDebounceTimer = setTimeout(function () {
          currentPage = 1;
          fetchFilteredProducts(true);
        }, 500);
      };

      minPriceInput.addEventListener('input', handlePriceInput);
      maxPriceInput.addEventListener('input', handlePriceInput);
    }

    // Sort select change
    if (sortSelect) {
      sortSelect.addEventListener('change', function () {
        currentPage = 1;
        fetchFilteredProducts(true);
      });
    }

    // Reset button
    if (desktopResetBtn) {
      desktopResetBtn.addEventListener('click', function () {
        resetAllFilters();
      });
    }

    if (mobileResetBtn) {
      mobileResetBtn.addEventListener('click', function () {
        resetAllFilters();
      });
    }
  }

  /**
   * Bind dynamic event listeners inside the rendered Thymeleaf fragment
   */
  function bindFragmentEventListeners() {
    // 1. Color Swatch Hover / Click Preview
    const cards = document.querySelectorAll('.apple-product-card');
    cards.forEach(function (card) {
      const img = card.querySelector('.product-preview-img');
      const dots = card.querySelectorAll('.swatch-dot');

      dots.forEach(function (dot) {
        const previewUrl = dot.getAttribute('data-img-src');
        if (!previewUrl || !img) return;

        const switchImage = function () {
          dots.forEach(d => d.classList.remove('active'));
          dot.classList.add('active');

          if (img.src !== previewUrl) {
            img.classList.add('img-switching');
            setTimeout(function () {
              img.src = previewUrl;
              img.classList.remove('img-switching');
            }, 150);
          }
        };

        dot.addEventListener('mouseenter', switchImage);
        dot.addEventListener('click', switchImage);
      });
    });

    // 2. Active Chip Removal
    const removeChipBtns = document.querySelectorAll('.btn-remove-chip');
    removeChipBtns.forEach(function (btn) {
      btn.addEventListener('click', function () {
        const type = btn.getAttribute('data-chip-type');
        const val = btn.getAttribute('data-chip-value');

        if (type === 'category') {
          const cb = document.querySelector(`.filter-category-checkbox[value="${val}"]`);
          if (cb) cb.checked = false;
        } else if (type === 'storage') {
          const cb = document.querySelector(`.filter-storage-checkbox[value="${val}"]`);
          if (cb) cb.checked = false;
        } else if (type === 'color') {
          const cb = document.querySelector(`.filter-color-checkbox[value="${val}"]`);
          if (cb) cb.checked = false;
        } else if (type === 'price') {
          if (minPriceInput) minPriceInput.value = '';
          if (maxPriceInput) maxPriceInput.value = '';
          quickPricePills.forEach(p => p.classList.remove('active'));
        }

        currentPage = 1;
        updateContextualFacets();
        fetchFilteredProducts(true);
      });
    });

    // 3. Clear all filters from chip bar or empty state
    const clearAllBtn = document.getElementById('clearAllFiltersBtn');
    if (clearAllBtn) {
      clearAllBtn.addEventListener('click', function () {
        resetAllFilters();
      });
    }

    const emptyStateResetBtn = document.getElementById('emptyStateResetBtn');
    if (emptyStateResetBtn) {
      emptyStateResetBtn.addEventListener('click', function () {
        resetAllFilters();
      });
    }

    // 4. Pagination navigation clicks
    const pageNavBtns = document.querySelectorAll('.page-nav-btn');
    pageNavBtns.forEach(function (btn) {
      btn.addEventListener('click', function () {
        const targetPage = parseInt(btn.getAttribute('data-page'), 10);
        if (!isNaN(targetPage) && targetPage > 0 && targetPage !== currentPage) {
          currentPage = targetPage;
          fetchFilteredProducts(true);
          gridContainer.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      });
    });
  }

  /**
   * Reset all filter controls
   */
  function resetAllFilters() {
    document.querySelectorAll('.filter-category-checkbox').forEach(cb => cb.checked = false);
    document.querySelectorAll('.filter-storage-checkbox').forEach(cb => cb.checked = false);
    document.querySelectorAll('.filter-color-checkbox').forEach(cb => cb.checked = false);
    quickPricePills.forEach(p => p.classList.remove('active'));

    if (minPriceInput) minPriceInput.value = '';
    if (maxPriceInput) maxPriceInput.value = '';
    if (sortSelect) sortSelect.value = 'newest';

    currentPage = 1;
    updateContextualFacets();
    fetchFilteredProducts(true);
  }

  /**
   * Hide/dim storage facet if non-storage categories (airpods, watch, phu-kien) are selected
   */
  function updateContextualFacets() {
    if (!storageFilterGroup) return;

    const checkedCategories = Array.from(
      document.querySelectorAll('.filter-category-checkbox:checked')
    ).map(cb => cb.value.toLowerCase());

    const noStorageCategories = ['airpods', 'watch', 'phu-kien'];

    if (
      checkedCategories.length > 0 &&
      checkedCategories.every(cat => noStorageCategories.includes(cat))
    ) {
      storageFilterGroup.style.opacity = '0.35';
      storageFilterGroup.style.pointerEvents = 'none';
      document.querySelectorAll('.filter-storage-checkbox').forEach(cb => cb.checked = false);
    } else {
      storageFilterGroup.style.opacity = '1';
      storageFilterGroup.style.pointerEvents = 'auto';
    }
  }

  /**
   * Build URL Query Params from active filters
   */
  function buildQueryParams() {
    const params = new URLSearchParams();

    // Categories
    const categories = Array.from(
      document.querySelectorAll('.filter-category-checkbox:checked')
    ).map(cb => cb.value);
    categories.forEach(cat => params.append('category', cat));

    // Price
    if (minPriceInput && minPriceInput.value.trim() !== '') {
      params.append('minPrice', minPriceInput.value.trim());
    }
    if (maxPriceInput && maxPriceInput.value.trim() !== '') {
      params.append('maxPrice', maxPriceInput.value.trim());
    }

    // Storages
    const storages = Array.from(
      document.querySelectorAll('.filter-storage-checkbox:checked')
    ).map(cb => cb.value);
    storages.forEach(st => params.append('storage', st));

    // Colors
    const colors = Array.from(
      document.querySelectorAll('.filter-color-checkbox:checked')
    ).map(cb => cb.value);
    colors.forEach(col => params.append('color', col));

    // Sort
    if (sortSelect && sortSelect.value) {
      params.append('sort', sortSelect.value);
    }

    // Page
    if (currentPage > 1) {
      params.append('page', currentPage.toString());
    }

    return params;
  }

  /**
   * Fetch filtered products fragment from server
   */
  function fetchFilteredProducts(updateHistory) {
    const params = buildQueryParams();
    const filterUrl = '/products/filter?' + params.toString();
    const pageUrl = '/products' + (params.toString() ? '?' + params.toString() : '');

    // Show loading state
    const overlay = document.getElementById('catalogLoadingOverlay');
    const cardsRow = document.getElementById('products-cards-row');
    if (overlay) overlay.classList.remove('d-none');
    if (cardsRow) cardsRow.classList.add('grid-fade-out');

    fetch(filterUrl, {
      headers: {
        'X-Requested-With': 'XMLHttpRequest'
      }
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.text();
      })
      .then(html => {
        gridContainer.innerHTML = html;
        bindFragmentEventListeners();

        if (updateHistory) {
          window.history.pushState({ path: pageUrl }, '', pageUrl);
        }
      })
      .catch(error => {
        console.error('Error fetching filtered products:', error);
      })
      .finally(() => {
        const updatedOverlay = document.getElementById('catalogLoadingOverlay');
        if (updatedOverlay) updatedOverlay.classList.add('d-none');
      });
  }

  /**
   * Synchronize input states when user navigates using browser Back/Forward
   */
  function syncInputsFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);

    // Sync categories
    const categories = urlParams.getAll('category');
    document.querySelectorAll('.filter-category-checkbox').forEach(cb => {
      cb.checked = categories.includes(cb.value);
    });

    // Sync price
    if (minPriceInput) minPriceInput.value = urlParams.get('minPrice') || '';
    if (maxPriceInput) maxPriceInput.value = urlParams.get('maxPrice') || '';

    // Sync storages
    const storages = urlParams.getAll('storage');
    document.querySelectorAll('.filter-storage-checkbox').forEach(cb => {
      cb.checked = storages.includes(cb.value);
    });

    // Sync colors
    const colors = urlParams.getAll('color');
    document.querySelectorAll('.filter-color-checkbox').forEach(cb => {
      cb.checked = colors.includes(cb.value);
    });

    // Sync sort
    if (sortSelect) sortSelect.value = urlParams.get('sort') || 'newest';

    // Sync page
    currentPage = parseInt(urlParams.get('page') || '1', 10);
    updateContextualFacets();
  }

  /**
   * Mobile offcanvas synchronization
   */
  function initMobileFilterSync() {
    if (!mobileFilterContent) return;

    const desktopForm = document.getElementById('filterForm');
    if (desktopForm) {
      mobileFilterContent.innerHTML = desktopForm.innerHTML;
    }

    // Two-way synchronization between mobile clone and desktop
    mobileFilterContent.addEventListener('change', function (e) {
      const name = e.target.name;
      const value = e.target.value;
      const checked = e.target.checked;

      const desktopInput = desktopForm.querySelector(`input[name="${name}"][value="${value}"]`);
      if (desktopInput) {
        desktopInput.checked = checked;
        desktopInput.dispatchEvent(new Event('change', { bubbles: true }));
      }
    });

    const mobileApplyBtn = document.getElementById('mobileApplyFiltersBtn');
    if (mobileApplyBtn) {
      mobileApplyBtn.addEventListener('click', function () {
        currentPage = 1;
        fetchFilteredProducts(true);
      });
    }
  }
});
