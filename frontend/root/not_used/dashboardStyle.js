var dashboardArray = document.getElementsByClassName("dashbuttons");
window.onload = function () {

    //console.log(dashboardArray.length);
    //console.log(dashboardArray.children)
  };

function resize(){
    for(const dash of dashboardArray ){
        console.log(dash.children.length);
//         l is the number of pixels to the left of that button
        let l = (window.innerWidth / (dash.children.length ));
//         let l = 400/(dash.children.length );
        //console.log(dash.length);
        //console.log(l)
        let counter = 0
        for(const child of dash.children){
            // TEMPORARY dummy hrefs TEMPORARY 
            child.setAttribute('href', "dummy.html");
            child.setAttribute("style", `margin-left:${l+10}px`);
            counter++;

        }
    }

};

resize(); 
window.onresize = resize();
