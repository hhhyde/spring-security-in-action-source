delete from `oauth_client_details` where `client_id`= 'client';
INSERT INTO `oauth_client_details` (`client_id`, `client_secret`,  `scope`, `authorized_grant_types`) VALUES ('client', 'secret', 'read', 'password,refresh_token');
