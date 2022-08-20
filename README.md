# Insider SDK Case Study

The SDK case study that uses a WebView to manage some stars, and also display a star image.

## Notes

As there were some information that I did not properly understand in the case study itself, they
will be noted [here](notes.txt)

## SDK Implementation

### Gradle Configuration

The SDK is published using a private maven server, called myMavenRepo. The gradle configuration is
as follows:

Inside **settings.gradle** file, under `dependencyResolutionManagement.repositories` (or with old
configuration, inside `allprojects` block using the project level **build.gradle** file), copy-paste
this:

```groovy
maven {
    url "https://mymavenrepo.com/repo/dmUaQmIJZ0G4koOlJE9W/"
    credentials {
        username "myMavenRepo"
        password "123456"
    }
}
```

Inside app level **build.gradle** file, under `dependencies` section, copy-paste this:

```groovy
dependencies {
    def web_view_star_version = "1.0.0"
    implementation "com.insider:webviewstar:$web_view_star_version"
}
```

### Code Implementation

Create an instance, passing the WebView, using `WebViewStarSDK.createInstance(webView)`, which will
load the necessary data and allow you to manipulate the big and small star data.

Sample code:

```java
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.furkanyurdakul.webviewstar.WebViewStarSDK;

public class MainActivity extends AppCompatActivity {

    private WebViewStarSDK sdk;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sdk = WebViewStarSDK.createInstance(findViewById(R.id.webView));

        // To add a big star
        sdk.addBigStar();

        // To add a small star
        sdk.addSmallStar();

        // To reset the stars state
        sdk.reset();
    }
}
```

Only the exposed methods should be used.

### Implementation Notes

The SDK will destroy itself once the associated WebView is no longer needed. That is, when the
WebView gets detached from the window, the SDK methods will be no-op. This is usually
at `Activity.onDestroy()` so you can create another SDK using the creator method again.

The star states are saved internally, so you do not need to do anything as long as
the SDK instance remains valid.

### Logging Data

You can use the tags **WebViewStarSDK** to check for console messages and check for
the star array state. They will only be logged on debug builds.