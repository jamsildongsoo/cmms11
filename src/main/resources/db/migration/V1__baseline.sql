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
  file_group_id VARCHAR(100),
  note       VARCHAR(500),
  delete_mark CHAR(1) DEFAULT 'N',
  created_at TIMESTAMP NULL,
  created_by CHAR(10),
  updated_at TIMESTAMP NULL,
  updated_by CHAR(10),
  CONSTRAINT pk_plant_master PRIMARY KEY (company_id, plant_id)
);

CREATE TABLE IF NOT EXISTS inspection (
  company_id CHAR(5) NOT NULL,
  inspection_id CHAR(10) NOT NULL,
  name VARCHAR(100),
  plant_id CHAR(10),
  job_id CHAR(5),
  site_id CHAR(5),
  dept_id CHAR(5),
  member_id CHAR(5),
  planned_date DATE,
  actual_date DATE,
  status CHAR(10),
  file_group_id VARCHAR(100),
  note VARCHAR(500),
  created_at TIMESTAMP NULL,
  created_by CHAR(10),
  updated_at TIMESTAMP NULL,
  updated_by CHAR(10),
  CONSTRAINT pk_inspection PRIMARY KEY (company_id, inspection_id)
);

CREATE TABLE IF NOT EXISTS inspection_item (
  company_id CHAR(5) NOT NULL,
  inspection_id CHAR(10) NOT NULL,
  line_no INTEGER NOT NULL,
  name VARCHAR(100),
  method VARCHAR(100),
  min_val VARCHAR(50),
  max_val VARCHAR(50),
  std_val VARCHAR(50),
  unit VARCHAR(50),
  result_val VARCHAR(50),
  note VARCHAR(500),
  CONSTRAINT pk_inspection_item PRIMARY KEY (company_id, inspection_id, line_no)
);

