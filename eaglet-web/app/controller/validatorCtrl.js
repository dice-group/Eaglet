app.controller("validatorCtrl", function($scope, eagletData) {
    $scope.directInput = "";
    $scope.tab = "directInput";
    $scope.file;
    $scope.postDirectInput = function(){

        console.log("direct Input");
        console.log($scope.directInput);
        $scope.tab = 'progress';
        //eagletData.postDirectInput($scope.directInput);
    }
    $scope.doUpload = function () {
        console.log("fileUpload");
        console.log($scope.file);
        $scope.tab = 'progress';
        //eagletData.uploadFile($scope.file);

    }
});