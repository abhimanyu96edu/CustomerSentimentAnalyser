package com.abhimanyusharma.customersentimentanalyser;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.abhimanyusharma.customersentimentanalyser.model.EntityInfo;
import com.abhimanyusharma.customersentimentanalyser.model.SentimentInfo;
import com.abhimanyusharma.customersentimentanalyser.model.TokenInfo;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.language.v1beta1.CloudNaturalLanguageAPI;
import com.google.api.services.language.v1beta1.CloudNaturalLanguageAPIRequest;
import com.google.api.services.language.v1beta1.CloudNaturalLanguageAPIScopes;
import com.google.api.services.language.v1beta1.model.AnalyzeEntitiesRequest;
import com.google.api.services.language.v1beta1.model.AnalyzeEntitiesResponse;
import com.google.api.services.language.v1beta1.model.AnalyzeSentimentRequest;
import com.google.api.services.language.v1beta1.model.AnalyzeSentimentResponse;
import com.google.api.services.language.v1beta1.model.AnnotateTextRequest;
import com.google.api.services.language.v1beta1.model.AnnotateTextResponse;
import com.google.api.services.language.v1beta1.model.Document;
import com.google.api.services.language.v1beta1.model.Entity;
import com.google.api.services.language.v1beta1.model.Features;
import com.google.api.services.language.v1beta1.model.Token;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ApiFragment extends Fragment {

    public interface Callback {

        void onEntitiesReady(EntityInfo[] entities);

        void onSentimentReady(SentimentInfo sentiment);

        void onSyntaxReady(TokenInfo[] tokens);
    }

    private static final String TAG = "ApiFragment";

    private GoogleCredential mCredential;

    private CloudNaturalLanguageAPI mApi = new CloudNaturalLanguageAPI.Builder(
            new NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                    mCredential.initialize(request);
                }
            }).build();

    private final BlockingQueue<CloudNaturalLanguageAPIRequest<? extends GenericJson>> mRequests
            = new ArrayBlockingQueue<>(3);

    private Thread mThread;

    private Callback mCallback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        final Fragment parent = getParentFragment();
        mCallback = parent != null ? (Callback) parent : (Callback) context;
    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }

    public void setAccessToken(String token) {
        mCredential = new GoogleCredential()
                .setAccessToken(token)
                .createScoped(CloudNaturalLanguageAPIScopes.all());
        startWorkerThread();
    }

    public void analyzeEntities(String text) {
        try {
            // Create a new entities API call request and add it to the task queue
            mRequests.add(mApi
                    .documents()
                    .analyzeEntities(new AnalyzeEntitiesRequest()
                            .setDocument(new Document()
                                    .setContent(text)
                                    .setType("PLAIN_TEXT"))));
        } catch (IOException e) {
            Log.e(TAG, "Failed to create analyze request.", e);
        }
    }

    public void analyzeSentiment(String text) {
        try {
            mRequests.add(mApi
                    .documents()
                    .analyzeSentiment(new AnalyzeSentimentRequest()
                            .setDocument(new Document()
                                    .setContent(text)
                                    .setType("PLAIN_TEXT"))));
        } catch (IOException e) {
            Log.e(TAG, "Failed to create analyze request.", e);
        }
    }

    public void analyzeSyntax(String text) {
        try {
            mRequests.add(mApi
                    .documents()
                    .annotateText(new AnnotateTextRequest()
                            .setDocument(new Document()
                                    .setContent(text)
                                    .setType("PLAIN_TEXT"))
                            .setFeatures(new Features()
                                    .setExtractSyntax(true))));
        } catch (IOException e) {
            Log.e(TAG, "Failed to create analyze request.", e);
        }
    }

    private void startWorkerThread() {
        if (mThread != null) {
            return;
        }
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (mThread == null) {
                        break;
                    }
                    try {
                        // API calls are executed here in this worker thread
                        deliverResponse(mRequests.take().execute());
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Interrupted.", e);
                        break;
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to execute a request.", e);
                    }
                }
            }
        });
        mThread.start();
    }

    private void deliverResponse(GenericJson response) {
        final Activity activity = getActivity();
        if (response instanceof AnalyzeEntitiesResponse) {
            final List<Entity> entities = ((AnalyzeEntitiesResponse) response).getEntities();
            final int size = entities.size();
            final EntityInfo[] array = new EntityInfo[size];
            for (int i = 0; i < size; i++) {
                array[i] = new EntityInfo(entities.get(i));
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onEntitiesReady(array);
                    }
                }
            });
        } else if (response instanceof AnalyzeSentimentResponse) {
            final SentimentInfo sentiment = new SentimentInfo(((AnalyzeSentimentResponse) response)
                    .getDocumentSentiment());
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onSentimentReady(sentiment);
                    }
                }
            });
        } else if (response instanceof AnnotateTextResponse) {
            final List<Token> tokens = ((AnnotateTextResponse) response).getTokens();
            final int size = tokens.size();
            final TokenInfo[] array = new TokenInfo[size];
            for (int i = 0; i < size; i++) {
                array[i] = new TokenInfo(tokens.get(i));
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onSyntaxReady(array);
                    }
                }
            });
        }
    }

}
