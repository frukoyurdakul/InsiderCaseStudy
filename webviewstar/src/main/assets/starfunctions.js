var stars = [];
var maxStarSize = 10;

class Random {
    static nextInt(seed) {
        return Math.floor(Math.random() * seed);
    }
}

class StarCreator {

    static getBrightness(randomValue) {
        return randomValue == 0 ? "Bright" : "Not so much";
    }

    static createBigStar() {
        var color = function(randomValue) {
            switch (randomValue) {
                case 0:
                    return "Yellow";
                case 1:
                    return "Purple";
                case 2:
                    return "Gray";
            }
        }
        return new Star("B", color(Random.nextInt(3)), StarCreator.getBrightness(Random.nextInt(2)));
    }

    static createSmallStar() {
        var color = function(randomValue) {
            switch (randomValue) {
                case 0:
                    return "Red";
                case 1:
                    return "Blue";
                case 2:
                    return "Green";
            }
        }
        return new Star("S", color(Random.nextInt(3)), StarCreator.getBrightness(Random.nextInt(2)));
    }

    static createStars(jsonString) {
        let model = JSON.parse(jsonString)
        let stars = [];
        model.forEach(item => {
            stars.push(new Star(item.size, item.color, item.brightness));
        });
        return stars;
    }
}

class Star {
    constructor(size, color, brightness) {
        this.size = size;
        this.color = color;
        this.brightness = brightness;
    }
}

function addStar(star) {
    if (stars.length == maxStarSize) {
        alert("Sky is full.");
    } else {
        stars.push(star);
        logToConsole();
    }
}

function logToConsole() {
    let starsString = JSON.stringify(stars);
    console.log(starsString);
    starsManager.onStarsChanged(starsString);

    var brightStars = 0;
    stars.forEach (item => {
        if (item.brightness == "Bright")
            brightStars++;
    })
    console.log("Bright stars count: " + brightStars + ", total stars count: " + stars.length);
}

function addBigStar() {
    addStar(StarCreator.createBigStar());
}

function addSmallStar() {
    addStar(StarCreator.createSmallStar());
}

function initializeStars(starsString) {
    stars = StarCreator.createStars(starsString);
    logToConsole();
}

function reset() {
    stars = [];
    logToConsole();
}