INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/remotevalidation/url', '', 0, 7211, 'n');
UPDATE Settings SET internal='n' WHERE name='system/inspire/enable';

UPDATE Settings SET datatype = 0, value = 'off' WHERE name = 'system/localrating/enable' and value = 'n';
UPDATE Settings SET datatype = 0, value = 'basic' WHERE name = 'system/localrating/enable' and value = 'y';

UPDATE Settings SET value='3.4.2' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';


