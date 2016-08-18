(function(window) {

  function toggleFormats() {
    var allRequests = document.querySelectorAll('.request-content');
    allRequests.forEach(function(requestContent) {
      var formattedButton = requestContent.querySelector('.show-formatted-request'),
          rawButton = requestContent.querySelector('.show-raw-request'),
          formattedRequest = requestContent.querySelector('.formatted-request'),
          rawRequest = requestContent.querySelector('.raw-request');

      function showRaw(formattedButton, rawRequest, formattedRequest) {
        rawButton.style.display = 'none';
        formattedButton.style.display = 'block';
        formattedRequest.style.display = 'none';
        rawRequest.style.display = 'block';
      }

      function showFormatted(formattedButton, rawButton, formattedRequest, rawRequest) {
        formattedButton.style.display = 'none';
        rawButton.style.display = 'block';
        rawRequest.style.display = 'none';
        formattedRequest.style.display = 'block';
      }

      rawButton.addEventListener('click', function() {
        showRaw(rawButton, formattedButton, rawRequest, formattedRequest)
      }, false);

      formattedButton.addEventListener('click', function() {
        showFormatted(formattedButton, rawButton, formattedRequest, rawRequest)
      }, false);
    })
  }

  window.addEventListener('load', toggleFormats, false);

})(window)
