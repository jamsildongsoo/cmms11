-- Align file_group_id column lengths to 10 characters across master/transaction tables
-- The ALTER statements will fail if existing data contains values longer than the new limit,
-- preventing silent truncation.

ALTER TABLE IF EXISTS plant
  MODIFY COLUMN file_group_id CHAR(10) NULL;

ALTER TABLE IF EXISTS plant_master
  MODIFY COLUMN file_group_id CHAR(10) NULL;

ALTER TABLE IF EXISTS inventory
  MODIFY COLUMN file_group_id CHAR(10) NULL;

ALTER TABLE IF EXISTS inventory_master
  MODIFY COLUMN file_group_id CHAR(10) NULL;

ALTER TABLE IF EXISTS inspection
  MODIFY COLUMN file_group_id CHAR(10) NULL;

ALTER TABLE IF EXISTS work_order
  MODIFY COLUMN file_group_id CHAR(10) NULL;

ALTER TABLE IF EXISTS work_permit
  MODIFY COLUMN file_group_id CHAR(10) NULL;

ALTER TABLE IF EXISTS memo
  MODIFY COLUMN file_group_id CHAR(10) NULL;

ALTER TABLE IF EXISTS approval
  MODIFY COLUMN file_group_id CHAR(10) NULL;

ALTER TABLE IF EXISTS file_group
  MODIFY COLUMN file_group_id CHAR(10) NOT NULL;

ALTER TABLE IF EXISTS file_item
  MODIFY COLUMN file_group_id CHAR(10) NOT NULL;
