(function() {

  goog.provide('gn_search_discover');

  goog.require('gn_search');
  goog.require('gn_search_discover_config');
  goog.require('gn_solr');

  var module = angular.module('gn_search_discover',
      ['gn_search', 'gn_search_discover_config', 'gn_solr']);


  module.controller('gnsDefault', ['$scope', '$http', 'gnSolrRequestManager',
    function($scope, $http, gnSolrRequestManager) {

      $scope.anyField = '';
      $scope.current = null;

      var facetConfig = {
        feature: 'parent',
        dataset: 'codelist_spatialRepresentationType',
        service: 'serviceType'
      };
      var searchObj =
        gnSolrRequestManager.register('Default', 'facets');
      searchObj.init();
      $scope.search = function () {
        searchObj.searchWithFacets(null, $scope.anyField, {
          facet: true,
          'facet.field': 'resourceType'
        }).then(function (resp) {
          $scope.results = resp;
        });
      };
      $scope.filter = function(filter, value) {
        var p = {}, v = {};
        v[value] = ''
        p[filter] = {values: v};

        var facet = {};
        if (facetConfig[value]) {
          facet = {
            facet: true,
            'facet.field': facetConfig[value]
          }
        }

        searchObj.searchWithFacets(p, $scope.anyField, facet).then(function (resp) {
          $scope.results = resp;
          console.log(resp);
        });
      };
      $scope.previous = function() {
        searchObj.previous(null, $scope.anyField);
      };
      $scope.next = function() {
        searchObj.next(null, $scope.anyField);
      };
      searchObj.on('search', function(resp) {
        $scope.results = resp;
      });
      $scope.setCurrent = function(r) {
        $scope.current = r;
        if ($scope.current.geom) {
          var format = new ol.format.WKT();
          var feature = format.readFeature($scope.current.geom[0]);
          feature.getGeometry().transform('EPSG:4326', 'EPSG:3857');
          vector.getSource().clear();
          vector.getSource().addFeature(feature);
          map.getView().fit(feature.getGeometry().getExtent(), map.getSize());
        }
      };

      var map = new ol.Map({
        view: new ol.View({
          center: [0, 0],
          zoom: 1
        }),
        layers: [
          new ol.layer.Tile({
            source: new ol.source.MapQuest({layer: 'osm'})
          })
        ],
        target: 'map'
      });


      var vector = new ol.layer.Vector({
        source: new ol.source.Vector({})
      });
      map.addLayer(vector);
    }]);
})();
