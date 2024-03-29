CREATE TABLE authbv.ACCESSTOKENS (
	`ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`ACCESS_TOKEN` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
	`REFRESH_TOKEN` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `ACCESS_TOKEN_EXPIRES_AT` datetime NOT NULL,
    `REFRESH_TOKEN_EXPIRES_AT` datetime NOT NULL,
    `CLIENT_ID` int(10) unsigned NOT NULL,
	`VERSION` int(10) DEFAULT 0,
	`DATE_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uk_accesstokens_1` (`ACCESS_TOKEN`),
  UNIQUE KEY `uk_accesstokens_2` (`REFRESH_TOKEN`),
  KEY `fk_accesstokens_clients` (`CLIENT_ID`),
  CONSTRAINT `fk_accesstokens_clients` FOREIGN KEY (`CLIENT_ID`) REFERENCES `CLIENTS` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
