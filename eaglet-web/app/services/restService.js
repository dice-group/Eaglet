'use strict';
angular.module('eagletApp.dataService', ['lr.upload'])
    .service('eagletData', ['$q', '$http', function ($q, $http, upload) {
        return {
            postDirectInput: function (String) {
                var def = $q.defer();
                var serverAdress = 'http://localhost:80';
                var path = '?'
                var req = {
                    method: 'POST',
                    url: serverAdress + path,
                    /*headers: {
                     'Content-Type': 'application/json'
                     },*/
                    data: {input: String}
                };
                $http(req).then(function (result) {
                    def.resolve(result)
                }, function (error) {
                    def.reject("error");
                });
                return def.promise;

            },
            uploadFile: function(file){
                var def = $q.defer();
                upload({
                    url: 'RESTPFAD',
                    method: 'POST',
                    data: {
                        file: file, // a jqLite type="file" element, upload() will extract all the files from the input and put them into the FormData object before sending.
                    }
                }).then(
                    function (result) {
                        def.resolve(result)
                    },
                    function (error) {
                        def.reject(error); //  Will return if status code is above 200 and lower than 300, same as $http
                    }
                );
                return def.promise;
            }
        }
    }
    ])