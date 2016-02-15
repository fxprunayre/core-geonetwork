(function() {

  goog.provide('gn_search_discover');

  goog.require('gn_search');
  goog.require('gn_search_discover_config');

  var module = angular.module('gn_search_discover',
      ['gn_search', 'gn_search_discover_config']);


  module.controller('gnsDefault', ['$scope', '$http',
    function($scope, $http) {

      $scope.anyField = '';
      $scope.current = null;
      $scope.search = function () {
        $scope.current = null;
        $http.get('../api/0.1/search/query', {params: {
          q: $scope.anyField || '*:*',
          wt: 'json',
          facet.field: 'docType',
          rows: 30
        }}).then(function (r) {
          $scope.results = r.data.response.docs;
        })
      };
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
