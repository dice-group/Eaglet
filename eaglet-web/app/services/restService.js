'use strict';
angular.module('eagletApp.dataService', ['lr.upload'])
    .service('eagletData', ['$q', '$http', function ($q, $http, upload) {
        return {
            postDirectInput: function (string) {
                var def = $q.defer();
                var serverAdress = 'http://127.0.0.1:8080/gscheck';
                var path = '/service/post-turtle-string'
                var req = {
                    method: 'POST',
                    url: serverAdress + path,
                    /*headers: {
                     'Content-Type': 'application/json'
                     },*/
                    turtle: string
                };
                console.log(req);
                $http(req).then(function (result) {
                    console.log(result);
                    def.resolve(result)
                }, function (error) {
                    console.log(error);
                    def.reject("error");
                });
                return def.promise;

            },
            uploadFile: function(file){
                var def = $q.defer();
                var serverAdress = 'http://127.0.0.1:8080/gscheck';
                var path = '/service/post-turtle-file'
                upload({
                    url: serverAdress + path,
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