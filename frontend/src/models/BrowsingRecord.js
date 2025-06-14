/**
 * 浏览记录模型类
 * 用于与后端交互时的数据结构
 * 添加默认构造函数，解决Jackson反序列化问题
 */
export class BrowsingRecordVO {
  constructor(recordId = null, productId = null, productName = '', productImage = '', viewedAt = null) {
    this.recordId = recordId;
    this.productId = productId;
    this.productName = productName;
    this.productImage = productImage;
    this.viewedAt = viewedAt || new Date().toISOString();
  }

  /**
   * 从JSON对象创建浏览记录实例
   * @param {Object} json JSON对象
   * @returns {BrowsingRecordVO} 浏览记录实例
   */
  static fromJson(json) {
    return new BrowsingRecordVO(
      json.recordId,
      json.productId,
      json.productName,
      json.productImage,
      json.viewedAt
    );
  }

  /**
   * 转换为JSON对象
   * @returns {Object} JSON对象
   */
  toJson() {
    return {
      recordId: this.recordId,
      productId: this.productId,
      productName: this.productName,
      productImage: this.productImage,
      viewedAt: this.viewedAt
    };
  }
}

export default BrowsingRecordVO; 