app.controller("validatorCtrl", function($scope, eagletData) {
    $scope.directInput = "";
    $scope.tab = "directInput";
    $scope.file;
    $scope.errorArray =[];
    $scope.postDirectInput = function(string){
        $scope.tab = 'progress';
        eagletData.postDirectInput(string).then(function(result){
            $scope.errorArray = result;
            $scope.tab = 'result';
        });
    }
    $scope.doUpload = function () {
        console.log("fileUpload");
        console.log($scope.file);
        $scope.tab = 'progress';
        //multipart file
        eagletData.uploadFile($scope.file);

    }
    $scope.parseResult= function(response){
        //console.log(response);
        $scope.errorArray = response.data;
        console.log($scope.errorArray);
        $scope.tab = 'result';
    }
    function formatError(json){

    }
});