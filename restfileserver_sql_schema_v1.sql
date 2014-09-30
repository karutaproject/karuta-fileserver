--
-- Create schema restfileserver_sql_schema_v1
--

CREATE DATABASE IF NOT EXISTS restfileserver_sql_v1;
USE restfileserver_sql_v1;

-- files table
-- file_uuid | user_id | file_name | file_data | deleted
DROP TABLE IF EXISTS `files_table`;
CREATE TABLE `files_table` (
	`file_uuid` char(36),
	`user_id` varchar(36),
    `file_name` varchar(128) DEFAULT "no-name file",
	`file_data` longblob,
	`deleted` int(1) unsigned NOT NULL default 0,
	PRIMARY KEY(file_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DELETE FROM `files_table`;

-- SELECT * FROM files_table;

-- SELECT `file_uuid`, `user_id`, `file_name` FROM files_table;

-- SELECT * FROM files_table WHERE file_uuid = "00000000-0000-002a-0000-00000000002a";

-- UPDATE files_table SET file_uuid = "00000000-0000-002a-0000-00000000002b" WHERE file_uuid = "00000000-0000-002a-0000-00000000002a"

	
