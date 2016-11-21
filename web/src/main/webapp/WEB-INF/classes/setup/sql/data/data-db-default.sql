

-- ======================================================================
-- === Table: Groups
-- ======================================================================

INSERT INTO Groups (id, name, description, email, referrer) VALUES (-1,'GUEST','self-registered users',NULL,NULL);
INSERT INTO Groups (id, name, description, email, referrer) VALUES (0,'intranet',NULL,NULL,NULL);
INSERT INTO Groups (id, name, description, email, referrer) VALUES (1,'all',NULL,NULL,NULL);
INSERT INTO Groups (id, name, description, email, referrer) VALUES (2,'sample',NULL,NULL,NULL);

-- ======================================================================
-- === Table: IsoLanguages
-- ======================================================================

INSERT INTO IsoLanguages (id, code, shortcode) VALUES  (137,'fre', 'fr');

INSERT INTO StatusValues (id, name, reserved, displayorder) VALUES  (0,'unknown','y', 0);
INSERT INTO StatusValues (id, name, reserved, displayorder) VALUES  (1,'draft','y', 1);
INSERT INTO StatusValues (id, name, reserved, displayorder) VALUES  (2,'approved','y', 3);
INSERT INTO StatusValues (id, name, reserved, displayorder) VALUES  (3,'retired','y', 5);
INSERT INTO StatusValues (id, name, reserved, displayorder) VALUES  (4,'submitted','y', 2);
INSERT INTO StatusValues (id, name, reserved, displayorder) VALUES  (5,'rejected','y', 4);

INSERT INTO Operations (id, name) VALUES  (0,'view');
INSERT INTO Operations (id, name) VALUES  (1,'download');
INSERT INTO Operations (id, name) VALUES  (2,'editing');
INSERT INTO Operations (id, name) VALUES  (3,'notify');
INSERT INTO Operations (id, name) VALUES  (5,'dynamic');
INSERT INTO Operations (id, name) VALUES  (6,'featured');


-- ======================================================================
-- === Table: Settings
-- ======================================================================

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/site/name', 'Catalogue SINP', 0, 110, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/site/siteId', '', 0, 120, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/site/organization', 'Ministère de l''Environnement, de l''Energie et de la Mer', 0, 130, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/platform/version', '3.2.1', 0, 150, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/platform/subVersion', 'SNAPSHOT', 0, 160, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/site/svnUuid', '', 0, 170, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/server/host', 'localhost', 0, 210, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/server/protocol', 'http', 0, 220, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/server/port', '8080', 1, 230, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/server/securePort', '8443', 1, 240, 'y');
INSERT INTO settings (name, value, datatype, position, internal) VALUES ('system/server/log','log4j.xml',0,250,'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/intranet/network', '127.0.0.1', 0, 310, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/intranet/netmask', '255.0.0.0', 0, 320, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/proxy/use', 'false', 2, 510, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/proxy/host', NULL, 0, 520, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/proxy/port', NULL, 1, 530, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/proxy/username', NULL, 0, 540, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/proxy/password', NULL, 0, 550, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/proxy/ignorehostlist', NULL, 0, 560, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/cors/allowedHosts', '*', 0, 561, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/email', 'root@localhost', 0, 610, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/host', '', 0, 630, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/port', '25', 1, 640, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/username', '', 0, 642, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/password', '', 0, 643, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/ssl', 'false', 2, 641, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/tls', 'false', 2, 644, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/ignoreSslCertificateErrors', 'false', 2, 645, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/selectionmanager/maxrecords', '1000', 1, 910, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/csw/enable', 'true', 2, 1210, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/csw/contactId', NULL, 0, 1220, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/csw/metadataPublic', 'false', 2, 1310, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/csw/transactionUpdateCreateXPath', 'true', 2, 1320, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userSelfRegistration/enable', 'false', 2, 1910, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userFeedback/enable', 'false', 2, 1911, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/clickablehyperlinks/enable', 'true', 2, 2010, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/localrating/enable', 'false', 2, 2110, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/downloadservice/leave', 'false', 0, 2210, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/downloadservice/simple', 'true', 0, 2220, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/downloadservice/withdisclaimer', 'false', 0, 2230, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/xlinkResolver/enable', 'false', 2, 2310, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/xlinkResolver/localXlinkEnable', 'true', 2, 2311, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/xlinkResolver/ignore', 'operatesOn,featureCatalogueCitation,Anchor,source', 0, 2312, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/hidewithheldelements/enableLogging', 'false', 2, 2320, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/autofixing/enable', 'true', 2, 2410, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/searchStats/enable', 'true', 2, 2510, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/indexoptimizer/enable', 'true', 2, 6010, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/indexoptimizer/at/hour', '0', 1, 6030, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/indexoptimizer/at/min', '0', 1, 6040, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/indexoptimizer/at/sec', '0', 1, 6050, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/indexoptimizer/interval', NULL, 0, 6060, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/indexoptimizer/interval/day', '0', 1, 6070, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/indexoptimizer/interval/hour', '24', 1, 6080, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/indexoptimizer/interval/min', '0', 1, 6090, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/oai/mdmode', '1', 0, 7010, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/oai/tokentimeout', '3600', 1, 7020, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/oai/cachesize', '60', 1, 7030, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/oai/maxrecords', '10', 1, 7040, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/enable', 'true', 2, 7210, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/enableSearchPanel', 'false', 2, 7220, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atom', 'disabled', 0, 7230, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atomSchedule', '0 0 0/24 ? * *', 0, 7240, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atomProtocol', 'INSPIRE-ATOM', 0, 7250, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvester/enableEditing', 'false', 2, 9010, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/recipient', NULL, 0, 9020, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/template', '', 0, 9021, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/templateError', 'There was an error on the harvesting: $$errorMsg$$', 0, 9022, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/templateWarning', '', 0, 9023, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/subject', '[$$harvesterType$$] $$harvesterName$$ finished harvesting', 0, 9024, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/enabled', 'false', 2, 9025, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/level1', 'false', 2, 9026, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/level2', 'false', 2, 9027, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/level3', 'false', 2, 9028, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/prefergrouplogo', 'true', 2, 9111, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/allThesaurus', 'false', 2, 9160, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadatacreate/generateUuid', 'true', 2, 9100, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadataprivs/usergrouponly', 'false', 2, 9180, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/threadedindexing/maxthreads', '1', 1, 9210, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/autodetect/enable', 'false', 2, 9510, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/requestedLanguage/only', 'prefer_locale', 0, 9530, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/requestedLanguage/sorted', 'false', 2, 9540, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/requestedLanguage/ignorechars', '', 0, 9590, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/requestedLanguage/preferUiLanguage', 'true', 2, 9595, 'y');


-- INSERT INTO Settings (name, value, datatype, position, internal) VALUES
--  ('map/backgroundChoices', '{"contextList": []}', 0, 9590, false);
INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('map/config', '{"viewerMap": "../../map/config-viewer.xml", "listOfServices": {"wms": [], "wmts": []}, "useOSM":true,"context":"","layer":{"url":"http://www2.demis.nl/mapserver/wms.asp?","layers":"Countries","version":"1.1.1"},"projection":"EPSG:3857","projectionList":[{"code":"EPSG:4326","label":"WGS84 (EPSG:4326)"},{"code":"EPSG:3857","label":"Google mercator (EPSG:3857)"}]}', 3, 9590, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/background', 'osm', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/width', '500', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/summaryWidth', '500', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/mapproj', 'EPSG:3857', 0, 9590, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('map/proj4js', '[{"code":"EPSG:2154","value":"+proj=lcc +lat_1=49 +lat_2=44 +lat_0=46.5 +lon_0=3 +x_0=700000 +y_0=6600000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs"}]', 3, 9591, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('map/isMapViewerEnabled', 'true', 2, 9592, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('map/is3DModeAllowed', 'false', 2, 9593, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('map/isSaveMapInCatalogAllowed', 'true', 2, 9594, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES
('map/bingKey', 'AnElW2Zqi4fI-9cYx1LHiQfokQ9GrNzcjOh_p_0hkO1yo78ba8zTLARcLBIf8H6D', 0, 9595, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('metadata/editor/schemaConfig', '{"iso19110":{"defaultTab":"default","displayToolTip":false,"related":{"display":true,"readonly":true,"categories":["dataset"]},"validation":{"display":true}},"iso19139":{"defaultTab":"default","displayToolTip":false,"related":{"display":true,"categories":[]},"suggestion":{"display":true},"validation":{"display":true}},"dublin-core":{"defaultTab":"default","related":{"display":true,"readonly":false,"categories":["parent","onlinesrc"]}}}', 3, 10000, 'n');


INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/resourceIdentifierPrefix', 'http://localhost:8080/geonetwork/srv/resources', 0, 10001, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/draftWhenInGroup', '', 0, 100002, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/allowPublishInvalidMd', 'true', 2, 100003, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/automaticUnpublishInvalidMd', 'false', 2, 100004, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/forceValidationOnMdSave', 'false', 2, 100005, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/ui/defaultView', 'default', 0, 10100, 'n');

INSERT INTO HarvesterSettings (id, parentid, name, value) VALUES  (1,NULL,'harvesting',NULL);

-- ======================================================================
-- === Table: Users
-- ======================================================================

INSERT INTO Users (id, username, password, name, surname, profile, kind, organisation, security, authtype, enabled) VALUES  (1,'admin','46e44386069f7cf0d4f2a420b9a2383a612f316e2024b0fe84052b0b96c479a23e8a0be8b90fb8c2','admin','admin',0,'','','','', true);
INSERT INTO Address (id, address, city, country, state, zip) VALUES  (1, '', '', '', '', '');
INSERT INTO UserAddress (userid, addressid) VALUES  (1, 1);


-- ======================================================================
-- === Table: MetadataURNTemplates
-- ======================================================================

INSERT INTO MetadataIdentifierTemplate (id, name, template, isprovided) VALUES  (0, 'Custom URN', '', 'y');
INSERT INTO MetadataIdentifierTemplate (id, name, template, isprovided) VALUES  (1, 'Autogenerated URN', '', 'y');
