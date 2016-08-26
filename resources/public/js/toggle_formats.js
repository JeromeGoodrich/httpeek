(function(window) {

  function toggleDisplay(elements) {
    elements.forEach(function(element) {
      if (element.style.display == "none") {
        element.style.display = "block";
      } else {
        element.style.display = "none";
      }
    });
  }

  function toggleFormats() {
    var allRequests = document.querySelectorAll(".request-content");
    allRequests.forEach(function(requestContent) {
      var formattedButton = requestContent.querySelector(".toggle-to-formatted"),
        rawButton = requestContent.querySelector(".toggle-to-raw"),
        formattedRequest = requestContent.querySelector(".formatted-content"),
        rawRequest = requestContent.querySelector(".raw-content"),
        elements = [formattedButton, rawButton, formattedRequest, rawRequest];

      rawButton.addEventListener("click", function() { toggleDisplay(elements); }, false);
      formattedButton.addEventListener("click", function() { toggleDisplay(elements); }, false);
    });
  }

  function toggleBody() {
    var requestBodies = document.querySelectorAll(".request-body");
    requestBodies.forEach(function(requestBody) {
      var rawBodyButton = requestBody.querySelector(".toggle-to-raw"),
        formattedBodyButton = requestBody.querySelector(".toggle-to-formatted"),
        rawBody = requestBody.querySelector(".raw-body"),
        formattedBody = requestBody.querySelector(".formatted-body"),
        elements = [rawBodyButton, formattedBodyButton, rawBody, formattedBody];

      if (rawBodyButton) {
        rawBodyButton.addEventListener("click", function() { toggleDisplay(elements); }, false);
        formattedBodyButton.addEventListener("click", function() { toggleDisplay(elements); }, false);
      }
    });
  }

  window.addEventListener("load", toggleFormats, false);
  window.addEventListener("load", toggleBody, false);

})(window);
