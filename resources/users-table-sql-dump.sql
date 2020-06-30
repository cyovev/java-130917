CREATE TABLE IF NOT EXISTS `users` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `created` datetime DEFAULT NULL,
  `modified` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;

TRUNCATE `users`;

INSERT INTO `users` (`id`, `first_name`, `last_name`, `email`, `created`, `modified`) VALUES
	(1, 'Christo', 'Yovev', 'cyovev@tvz.hr', '2020-06-20 18:40:04', '2020-06-30 17:53:27'),
	(2, 'Hello', 'World', 'hello@world.com', '2020-06-24 23:14:55', '2020-06-26 00:02:12'),
	(3, 'Random', 'User', 'example@example.com', '2020-06-30 21:36:36', '2020-06-30 21:36:36');
