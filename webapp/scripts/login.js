function login() {
    var email = document.getElementById("email").value;
    var password = document.getElementById("password").value;
  
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "LoginServlet", true);
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    xhr.onreadystatechange = function () {
      if (xhr.readyState === 4) {
        if (xhr.status === 200) {
          var response = JSON.parse(xhr.responseText);
          if (response.success) {
            // Redirect to movie-list.html on successful login
            window.location.href = "../public/movie-list.html";
          } else {
            document.getElementById("error-message").innerText = response.message;
          }
        } else {
          document.getElementById("error-message").innerText = "Error occurred during login.";
        }
      }
    };
    xhr.send("email=" + email + "&password=" + password);
  }
  