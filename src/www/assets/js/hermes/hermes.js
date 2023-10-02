document.addEventListener("DOMContentLoaded", () => {
    const elementsWithHermesSrc = document.querySelectorAll('[hermes-src]');

    elementsWithHermesSrc.forEach(element => {
        const resource = element.getAttribute('hermes-src');
        const dataType = getImageType(resource);
        console.debug("requested resource: " + resource);
        console.log("resource type: " + JSON.stringify(dataType));
        fetch(resource + '?hermes=true')
            .then((response) => {
                const reader = response.body.getReader();
                let imageData = [];
                return new ReadableStream({
                    async start(controller) {
                        try {
                            while (true) {
                                const { done, value } = await reader.read();
                                if (done) {
                                    imageData[0] = imageData[0].slice(1);
                                    //console.log(imageData[0]);
                                    const blob = new Blob(imageData, { type: dataType.ext });
                                    const imageUrl = URL.createObjectURL(blob);
                                    /*console.log(imageUrl);
                                    element.src = imageUrl;
                                    console.log(element);
                                    controller.close();*/
                                    //element.addEventListener('load', () => URL.revokeObjectURL(imageUrl));
                                    element.src = imageUrl;
                                    console.log("done");
                                    controller.close();
                                    break;
                                }
                                imageData.push(value);
                            }
                        } catch (error) {
                            console.error("error processing chunks: ", error);
                        }
                        /*start(controller) {
                            return pump();
    
                            function pump() {
                                return reader.read().then(({ done, value }) => {
                                    // When no more data needs to be consumed, close the stream
                                    if (done) {
                                        controller.close();
                                        return;
                                    }
                                    // Enqueue the next data chunk into our target stream
                                    controller.enqueue(value);
                                    return pump();
                                });
                            }
    
                            while (true) {
                                const { done, value } = await reader.read();
                                if (done) {
                                    console.log("Stream done");
                                    break;
                                }
                                //console.debug("VALUE: " +value);
                                imageData.push(value);
                                
                                
                            }
                           
                            const concatenatedData = new Uint8Array(imageData.reduce((acc, chunk) => acc.concat(Array.from(chunk)), []));
    
                            console.log("hello");
                            const blob = new Blob(imageData, {type: 'image/png'});
                            const imageUrl = URL.createObjectURL(blob);
                            element.src = imageUrl;
                            console.log("close");
                            controller.close()*/
                    },
                });
            })
            //.then((stream) => new Response(stream))
            //.then((response) => response.blob())
            //.then((blob) => URL.createObjectURL(blob))
            // Update image
            /*.then((url) => {
                element.src = url;
                console.log(element);
            })*/
            .catch(error => {
                console.error("Error: ", error);
            });
    });

    function isValidJson(data) {
        try {
            JSON.parse(data);
            return true;
        } catch (error) {
            return false;
        }
    }

    function getImageType(filename) {
        const extensionMatch = /\.([a-zA-Z0-9]+)$/.exec(filename);
        console.log(extensionMatch)
        if (extensionMatch && extensionMatch[1]) {
            // Convert the extension to lowercase for comparison
            const extension = extensionMatch[1].toLowerCase();

            // List of known image extensions (you can expand this list as needed)
            const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg'];

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

            return imageTypes.find((it) => it.ext === extension );
        }
    }

});