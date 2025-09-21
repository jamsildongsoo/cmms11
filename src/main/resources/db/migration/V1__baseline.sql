-- Baseline schema for minimal CMMS11 (domain + master subset)

CREATE TABLE IF NOT EXISTS company (
  company_id  CHAR(5) PRIMARY KEY,
  name        VARCHAR(100),
  note        VARCHAR(500),
  delete_mark CHAR(1) DEFAULT 'N',
  created_at  TIMESTAMP NULL,
  created_by  CHAR(10),
  updated_at  TIMESTAMP NULL,
  updated_by  CHAR(10)
);

CREATE TABLE IF NOT EXISTS member (
  company_id CHAR(5) NOT NULL,
  member_id  CHAR(5) NOT NULL,
  name       VARCHAR(100),
  dept_id    CHAR(5),
  password_hash VARCHAR(100),
  email VARCHAR(100),
  phone VARCHAR(100),
  note       VARCHAR(500),
  delete_mark CHAR(1) DEFAULT 'N',
  created_at TIMESTAMP NULL,
  created_by CHAR(10),
  updated_at TIMESTAMP NULL,
  updated_by CHAR(10),
  CONSTRAINT pk_member PRIMARY KEY (company_id, member_id)
);

CREATE TABLE IF NOT EXISTS plant_master (
  company_id CHAR(5) NOT NULL,
  plant_id   CHAR(10) NOT NULL,
  name       VARCHAR(100),
  asset_id   CHAR(5),
  site_id    CHAR(5),
  dept_id    CHAR(5),
  func_id    CHAR(5),
  maker_name VARCHAR(100),
  spec       VARCHAR(100),
  model      VARCHAR(100),
  serial     VARCHAR(100),
  install_date DATE,
  depre_id   CHAR(5),
  depre_period SMALLINT,
  purchase_cost DECIMAL(18,2),
  residual_value DECIMAL(18,2),
  inspection_yn CHAR(1),
  psm_yn CHAR(1),
  workpermit_yn CHAR(1),
  inspection_interval SMALLINT,
  last_inspection DATE,
  next_inspection DATE,
  file_group_id CHAR(10),
  note       VARCHAR(500),
  delete_mark CHAR(1) DEFAULT 'N',
  created_at TIMESTAMP NULL,
  created_by CHAR(10),
  updated_at TIMESTAMP NULL,
  updated_by CHAR(10),
  CONSTRAINT pk_plant_master PRIMARY KEY (company_id, plant_id)
);

CREATE TABLE IF NOT EXISTS inventory_closing (
  company_id   CHAR(5) NOT NULL,
  yyyymm       CHAR(6) NOT NULL,
  storage_id   CHAR(5) NOT NULL,
  inventory_id CHAR(10) NOT NULL,
  begin_qty    DECIMAL(18,3),
  begin_amount DECIMAL(18,2),
  in_qty       DECIMAL(18,3),
  in_amount    DECIMAL(18,2),
  out_qty      DECIMAL(18,3),
  out_amount   DECIMAL(18,2),
  move_qty     DECIMAL(18,3),
  move_amount  DECIMAL(18,2),
  adj_qty      DECIMAL(18,3),
  adj_amount   DECIMAL(18,2),
  end_qty      DECIMAL(18,3),
  end_amount   DECIMAL(18,2),
  status       CHAR(5),
  closed_at    TIMESTAMP NULL,
  closed_by    CHAR(10),
  CONSTRAINT pk_inventory_closing PRIMARY KEY (company_id, yyyymm, storage_id, inventory_id)
);

