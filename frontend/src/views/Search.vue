<style scoped>
.search-container {
  max-width: 1400px;
  margin: 20px auto;
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding: 15px 25px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-header:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.search-title {
  font-size: 24px;
  font-weight: 600;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-title .el-icon {
  font-size: 28px;
  color: rgba(255, 255, 255, 0.9);
}

.search-stats {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.search-content {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 30px;
}

.filter-sidebar {
  position: sticky;
  top: 20px;
  height: fit-content;
}

.filter-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 25px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.filter-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.filter-section {
  margin-bottom: 25px;
}

.filter-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.price-range {
  display: flex;
  gap: 10px;
  align-items: center;
}

.price-input {
  flex: 1;
}

.price-input .el-input {
  --el-input-bg-color: rgba(255, 255, 255, 0.1);
  --el-input-border-color: rgba(255, 255, 255, 0.2);
  --el-input-hover-border-color: rgba(255, 255, 255, 0.3);
  --el-input-focus-border-color: rgba(255, 255, 255, 0.4);
  --el-input-text-color: #fff;
  --el-input-placeholder-color: rgba(255, 255, 255, 0.6);
}

.price-input .el-input__wrapper {
  box-shadow: none !important;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.price-input .el-input__wrapper:hover {
  background: rgba(255, 255, 255, 0.15);
}

.price-input .el-input__wrapper.is-focus {
  background: rgba(255, 255, 255, 0.2);
}

.price-separator {
  color: rgba(255, 255, 255, 0.6);
  font-size: 16px;
}

.brand-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 200px;
  overflow-y: auto;
  padding-right: 10px;
}

.brand-list::-webkit-scrollbar {
  width: 4px;
}

.brand-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.brand-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}

.brand-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.3s ease;
}

.brand-item:hover {
  color: #fff;
  transform: translateX(5px);
}

.brand-checkbox {
  --el-checkbox-checked-bg-color: rgba(255, 255, 255, 0.2);
  --el-checkbox-checked-border-color: rgba(255, 255, 255, 0.3);
}

.filter-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.filter-button {
  flex: 1;
  height: 36px;
  font-size: 14px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  transition: all 0.3s ease;
}

.filter-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.filter-button:active {
  transform: translateY(0);
}

.search-main {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 25px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.sort-bar:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.sort-options {
  display: flex;
  gap: 15px;
}

.sort-option {
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 5px 10px;
  border-radius: 6px;
}

.sort-option:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.sort-option.active {
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
}

.view-options {
  display: flex;
  gap: 10px;
}

.view-option {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 6px;
}

.view-option:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.view-option.active {
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.product-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.product-image {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
}

.product-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.4s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-info {
  padding: 20px;
}

.product-name {
  font-size: 16px;
  font-weight: 500;
  color: #fff;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.product-brand {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #ffd700;
}

.product-price {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 15px;
}

.product-actions {
  display: flex;
  gap: 10px;
}

.action-button {
  flex: 1;
  height: 36px;
  font-size: 14px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  transition: all 0.3s ease;
}

.action-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.action-button:active {
  transform: translateY(0);
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.pagination-container .el-pagination {
  --el-pagination-bg-color: rgba(255, 255, 255, 0.1);
  --el-pagination-hover-color: #fff;
  --el-pagination-button-color: rgba(255, 255, 255, 0.8);
  --el-pagination-button-bg-color: rgba(255, 255, 255, 0.1);
  --el-pagination-button-disabled-color: rgba(255, 255, 255, 0.4);
  --el-pagination-button-disabled-bg-color: rgba(255, 255, 255, 0.05);
}

.pagination-container .el-pagination .el-pager li {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.pagination-container .el-pagination .el-pager li:hover {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.pagination-container .el-pagination .el-pager li.active {
  background: rgba(255, 255, 255, 0.3);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.4);
}

@media (max-width: 1200px) {
  .search-content {
    grid-template-columns: 1fr;
  }
  
  .filter-sidebar {
    position: static;
  }
  
  .filter-card {
    margin-bottom: 25px;
  }
}

@media (max-width: 768px) {
  .search-container {
    padding: 15px;
  }
  
  .search-header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .sort-bar {
    flex-direction: column;
    gap: 15px;
  }
  
  .sort-options {
    width: 100%;
    justify-content: center;
  }
  
  .view-options {
    width: 100%;
    justify-content: center;
  }
  
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
}

@media (max-width: 576px) {
  .search-container {
    padding: 10px;
  }
  
  .search-title {
    font-size: 20px;
  }
  
  .search-stats {
    font-size: 13px;
  }
  
  .section-title {
    font-size: 16px;
  }
  
  .product-name {
    font-size: 15px;
  }
  
  .product-brand {
    font-size: 13px;
  }
  
  .product-price {
    font-size: 16px;
  }
  
  .action-button {
    height: 32px;
    font-size: 13px;
  }
}
</style> 