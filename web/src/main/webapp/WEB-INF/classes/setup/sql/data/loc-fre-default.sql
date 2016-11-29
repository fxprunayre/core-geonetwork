INSERT INTO Languages (id, name, isinspire, isdefault) VALUES ('fre','français', 'y', 'n');

-- Take care to table ID (related to other loc files)
INSERT INTO CswServerCapabilitiesInfo (idfield, langid, field, label) VALUES (21, 'fre', 'title', 'SINP - Système d''Information sur la Nature et les Paysages');
INSERT INTO CswServerCapabilitiesInfo (idfield, langid, field, label) VALUES (22, 'fre', 'abstract', 'En recensant les dispositifs de collecte, les bases de données associées et les acteurs impliqués, cet outil fournit une vision globale des données existantes dans le domaine de la nature et des paysages (espèces, habitats, espaces naturels...), de leurs modalités de production et de stockage, de leur accessibilité. Son périmètre à vocation à couvrir aussi bien la métropole que l''ensemble de l''outremer.');
INSERT INTO CswServerCapabilitiesInfo (idfield, langid, field, label) VALUES (23, 'fre', 'fees', 'none');
INSERT INTO CswServerCapabilitiesInfo (idfield, langid, field, label) VALUES (24, 'fre', 'accessConstraints', 'none');

INSERT INTO GroupsDes (iddes, langid, label) VALUES (-1,'fre','Invité');
INSERT INTO GroupsDes (iddes, langid, label) VALUES (0,'fre','Intranet');
INSERT INTO GroupsDes (iddes, langid, label) VALUES (1,'fre','Tous');
INSERT INTO GroupsDes (iddes, langid, label) VALUES (2,'fre','IDCNP');

INSERT INTO IsoLanguagesDes  (iddes, langid, label) VALUES (137,'fre','Français');

INSERT INTO OperationsDes  (iddes, langid, label) VALUES (0,'fre','Publier');
INSERT INTO OperationsDes  (iddes, langid, label) VALUES (1,'fre','Télécharger');
INSERT INTO OperationsDes  (iddes, langid, label) VALUES (2,'fre','Editer');
INSERT INTO OperationsDes  (iddes, langid, label) VALUES (3,'fre','Notifier');
INSERT INTO OperationsDes  (iddes, langid, label) VALUES (5,'fre','Carte interactive');
INSERT INTO OperationsDes  (iddes, langid, label) VALUES (6,'fre','Epingler');

INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (0,'fre','Inconnu');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (1,'fre','Brouillon');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (2,'fre','Validé');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (3,'fre','Retiré');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (4,'fre','A valider');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (5,'fre','Rejeté');

