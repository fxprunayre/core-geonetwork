/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

(function() {
  goog.provide('gn_importxsl_directive');

  var module = angular.module('gn_importxsl_directive', []);

  /**
   * Provide a list of available XSLT transformation
   *
   */
  module.directive('gnImportXsl', ['$http', '$translate',
    function($http, $translate) {

      return {
        restrict: 'A',
        replace: true,
        transclude: true,
        scope: {
          element: '=gnImportXsl'
        },
        templateUrl: '../../catalog/components/admin/importxsl/partials/' +
            'importxsl.html',
        link: function(scope, element, attrs) {
          $http.get('admin.harvester.info?' +
              'type=importStylesheets&_content_type=json')
              .success(function(data) {
                scope.stylesheets = data[0];
                scope.stylesheets.unshift({
                  id: '',
                  name: ''
                });
                scope.element = '';
              });
        }
      };
    }]);
})();
