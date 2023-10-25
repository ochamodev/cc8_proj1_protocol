function getImageType(filename) {
    const extensionMatch = /\.([a-zA-Z0-9]+)$/.exec(filename);
    console.log(extensionMatch)
    if (extensionMatch && extensionMatch[1]) {
        // Convert the extension to lowercase for comparison
        const extension = extensionMatch[1].toLowerCase();

        const imageTypes = [
            {
                'ext': 'jpg',
                'type': 'image/jpg'
            },
            {
                'ext': 'jpeg',
                'type': 'image/jpeg'
            },
            {
                'ext': 'png',
                'type': 'image/png'
            },
            {
                'ext': 'gif',
                'type': 'image/gif'
            },
            {
                'ext': 'bmp',
                'type': 'image/bmp'
            },
            {
                'ext': 'svg',
                'type': 'image/svg'
            }
        ];

        return imageTypes.find((it) => it.ext === extension);
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const elementsWithHermesSrc = document.querySelectorAll('[hermes-src]');
    const rowCountHeader = "hermes-chunk-row-count";
    const colCountHeader = "hermes-chunk-col-count";
    const chunkLocationHeader = "hermes-chunk-src";
    const chunkNameHeader = "hermes-chunk-name";

    elementsWithHermesSrc.forEach(async element => {
        const resource = element.getAttribute('hermes-src');
        const dataType = getImageType(resource);
        console.log("dataType ", dataType)
        console.log("heigth: " + element.clientHeight);
        console.log("width: " + element.clientWidth);
        console.log("divWidth = " + element.clientWidth)
        console.log("divHeight = " + element.clientHeight)
        let elementWidth = element.clientWidth;
        let elementHeight = element.clientWidth;

        console.log("elementWidth = ", elementWidth);
        console.log("elementHeight = ", elementHeight);

        let response = await fetch(resource + '?hermes=true&hermesStep=1')
        if (response.ok) {
            let headers = response.headers;
            let rowCount = headers.get(rowCountHeader);
            let colCount = headers.get(colCountHeader);
            let chunkSrc = headers.get(chunkLocationHeader);
            let chunkName = headers.get(chunkNameHeader);

            let chunkWidth = elementWidth / colCount;
            
            let chunkHeight = elementHeight / rowCount;
            for (let row = 0; row < rowCount; row++) {
                for (let col = 0; col < colCount; col++) {
                    let imagePath = `${chunkSrc}${chunkName}${row}_${col}.${dataType.ext}?hermes=true&hermesStep=2'`;
                    let img = new Image();
                    img.src = imagePath;
                    img.style.margin = 0;
                    img.style.padding = 0;
                    img.width = chunkWidth;

                    if (chunkHeight < 40) {
                        img.height = 40;
                    }
                    if (chunkWidth < 20) {
                        img.width = 16;
                    }

                    img.setAttribute("hermes-row-count", `${rowCount}`);
                    img.setAttribute("hermes-col-count", `${colCount}`);
                    element.appendChild(img);
                }
            }
        }

    });



});
var rtime;
var timeout = false;
var delta = 200;
window.onresize = function () {
    console.log("Se ha detectado un resize en el viewport");
    if (timeout == false) {
        timeout = true;
        setTimeout(resizeImages, delta);
    }

}

function resizeImages() {
    if (new Date() - rtime < delta) {
        setTimeout(resizeImages, delta);
    } else {
        timeout = false;
        console.log("done resizing");
        const elementsWithHermesSrc = document.querySelectorAll('[hermes-src]');

        elementsWithHermesSrc.forEach(element => {
            let elementWidth = element.clientWidth;
            let elementHeight = element.clientWidth;
            for (let img of element.children) {
                let rowCount = img.getAttribute("hermes-row-count");
                let colCount = img.getAttribute("hermes-col-count");
                let chunkWidth = elementWidth / colCount;
                let chunkHeight = elementHeight / rowCount;

                for (let row = 0; row < rowCount; row++) {
                    for (let col = 0; col < colCount; col++) {
                        img.width = chunkWidth;

                        if (chunkHeight < 40) {
                            img.height = 40;
                        }
                        if (chunkWidth < 20) {
                            img.width = 20;
                        }
                        img.style.margin = 0;
                        img.style.padding = 0;
                    }
                }
            }


        })
    }
}

document.onresize = function () {
    console.log("Se ha detectado un resize en el documento")
    const elementsWithHermesSrc = document.querySelectorAll('[hermes-src]');

    elementsWithHermesSrc.forEach(element => {
        let elementWidth = element.clientWidth;
        let elementHeight = element.clientWidth;

        for (let row = 0; row < rowCount; row++) {
            for (let col = 0; col < colCount; col++) {
                let imagePath = `${chunkSrc}${chunkName}${row}_${col}.${dataType.ext}`;
                let img = new Image(chunkSize, chunkSize);
                img.src = imagePath;
                img.style.margin = 0;
                img.style.padding = 0;
                img.setAttribute("hermes-row-count", `${rowCount}`);
                img.setAttribute("hermes-col-count", `${colCount}`);
                element.appendChild(img);
            }
        }

    })

}