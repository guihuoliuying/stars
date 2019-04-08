package com.stars.modules.task;


public class TaskSQL {
	/**
	 * CREATE TABLE `roleaccepttask` (
  		`roleid` bigint(20) NOT NULL,
  		`taskid` int(11) NOT NULL,
  		`process` varchar(24) DEFAULT NULL,
  		PRIMARY KEY (roleid,taskid)
		) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	 *
	 *
	 *
CREATE TABLE `mission` (
  `id` int(6) NOT NULL,
  `name` varchar(24) DEFAULT NULL,
  `desc` varchar(64) DEFAULT NULL,
  `dungeontips` varchar(64) DEFAULT NULL,
  `sort` tinyint(2) DEFAULT NULL,
  `target` varchar(24) NOT NULL,
  `award` varchar(24) NOT NULL,
  `autoget` tinyint(2) DEFAULT NULL,
  `autoaccept` tinyint(2) DEFAULT NULL,
  `jump` varchar(24) DEFAULT NULL,
  `add` int(6) DEFAULT NULL,
  `getgroup` int(6) DEFAULT NULL,
  `condition` varchar(24) DEFAULT NULL,
  `reset` tinyint(2) DEFAULT NULL,
  `prior` int(6) DEFAULT NULL,
  `event` varchar(24) DEFAULT NULL,
  `type` tinyint(2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	 *
	 *
	 *
	 */
}
