// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Initializes the dropdown menu on the top fixed navbar.
 */
$(document).ready(function () {
  $(".dropdown-trigger").dropdown();
  $('.tabs').tabs();

  
});

        
/**
 * Adds a random fun fact about me on each click.
 */

function randomFunFacts() {
  const funFacts = [
    'I"m a  Rising sophomore Computer Science major',
    "This is my first internship",
    "I was raised and high-schooled in Lagos, Nigeria",
    "I speak three languages: English, and two native lanuages, Yoruba and Efik",
  ];

  // Picking a random fact.
  const myFact = funFacts[Math.floor(Math.random() * funFacts.length)];

  // Add it to the page.
  const factBox = document.getElementById("factBox");
  factBox.innerHTML = myFact;
}

var slides = document.querySelectorAll("#slides .image-box");
var currentSlide = 0;
var slideInterval = setInterval(nextSlide, 3000);

function nextSlide() {
  slides[currentSlide].className = "image-box";
  currentSlide = (currentSlide + 1) % slides.length;
  slides[currentSlide].className = "image-box current";
}

function changeColor(color) {
  color_boxes = document.querySelectorAll("#color-id");
  for (var i = 0; i < color_boxes.length; i++) {
    color_boxes[i].style.backgroundColor = color;
    if (i == 1) {
        color_boxes[i].style.color = color;
    }
  }
}

function changeFont(font) {
  var bodyContainer = document.getElementsByTagName("body");
  for (var i = 0; i < bodyContainer.length; i++) {
    bodyContainer[i].style.fontStyle = font;
  }
}

function changePattern(pattern) {
  var bodyContainer = document.getElementsByTagName("body");
  for (var i = 0; i < bodyContainer.length; i++) {
    if (pattern == "one") {
      bodyContainer[i].style.backgroundImage = "url(/images/bg1.png)";
    } else if (pattern == "two") {
      bodyContainer[i].style.backgroundImage = "url(/images/bg2.png)";
    } else if (pattern == "three") {
      bodyContainer[i].style.backgroundImage = "url(/images/bg3.png)";
    }
  }
}
