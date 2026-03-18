CREATE TABLE web_marketplace_listings (
  id BIGINT NOT NULL AUTO_INCREMENT,
  mode ENUM('SELL','BUY') NOT NULL,
  char_id INT NOT NULL,
  item_id INT NOT NULL,
  enchant_level INT NOT NULL DEFAULT 0,
  count BIGINT NOT NULL,
  price BIGINT NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_mode_updated (mode, updated_at),
  INDEX idx_item (item_id),
  INDEX idx_char (char_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;