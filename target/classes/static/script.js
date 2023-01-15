let right = '<div id = "res"><img src="./right.png" style="width:65px; height: 50px; margin 2px">';
let wrong = '<div id = "res"><img src="./wrong.png" style="width:65px; height: 50px; margin 2px">';
let type = 1;


async function test() {
    // if(type == 0)
    //     return;
    const d = new Date();
    let minutes = d.getMinutes();
    let seconds = d.getSeconds();
    let time = '<br>'+minutes +"m "+ seconds+"s </div>";
    var str = document.getElementById("log").innerHTML;
    let g ;
    await fetch('http://localhost:8080/api')
    .then((response) => g = response.status);
    console.log("Output" +g);

    if(g == 200)
    document.getElementById("log").innerHTML = right + time + str;
    else 
    document.getElementById("log").innerHTML = wrong + time + str;
}

async function algorithm(event, currType) {
    let bucket = document.getElementById("input1").value;
    let refillRt = document.getElementById("input2").value;
    console.log(bucket+"  "+refillRt);
    let body = {type : currType, bucketSize : bucket, refillRate : refillRt};
    if(type == currType)
        return;
    else {

        $.ajax({url: "http://localhost:8080/algorithm", 
            method : 'POST',
            data : JSON.stringify(body),
            headers : {
                'content-type' : 'application/json'
            },
        });
        document.getElementById("log").innerHTML = "";
        console.log(event);
        const d = document.getElementById("tabs").childNodes[currType];
        console.log(d);
        document.getElementById("A"+currType).className ="flex-sm-fill text-sm-center nav-link active";
        if(type > 0)
        document.getElementById("A"+type).className="flex-sm-fill text-sm-center nav-link";
        type = currType;
    }
}


function setup() {
    let bucket = document.getElementById("input1").value;
    let refillRt = document.getElementById("input2").value;
    console.log(bucket+"  "+refillRt);
    let body = {type : currType, bucketSize : bucket, refillRate : refillRt};
    $.ajax({url: "http://localhost:8080/algorithm", 
        method : 'POST',
        data : JSON.stringify(body),
        headers : {
            'content-type' : 'application/json'
        },
    });
}

function clear() {
    document.getElementById("log").innerHTML = "";
}