package com.m.androidNativeApp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.os.Bundle;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.m.androidNativeApp.Client.apolloClient;

public class MainActivity extends AppCompatActivity {

    private String taskTitle, taskDescription, taskId;
    RecyclerView recyclerView;
    public ItemAdapter itemAdapter;
    List<Item> itemList;
    Item itemToRemove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemAdapter = new ItemAdapter(this, itemList);


        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerView.setAdapter(itemAdapter);
            }
        });

        getTasks();
        subscribeToAddTask();
        subscribeToDeleteTask();
    }

    public void subscribeToDeleteTask() {
        DeleteTaskSubscription deleteTaskSubscription = DeleteTaskSubscription
                .builder()
                .build();

        apolloClient.subscribe(deleteTaskSubscription).execute(new ApolloSubscriptionCall.Callback<DeleteTaskSubscription.Data>() {
            @Override
            public void onResponse(@NotNull Response<DeleteTaskSubscription.Data> response) {

                for (Item item : itemList) {
                    if (item.getId().equals(response.data().taskDeleted.fragments().taskFields.id())) {
                        itemToRemove = item;
                    }
                }

                itemList.remove(itemToRemove);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        itemAdapter.notifyDataSetChanged();
                    }
                });

            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                System.out.println("Failed" + e);
            }

            @Override
            public void onCompleted() {
                System.out.println("Subscribed to DeleteTask");
            }

            @Override
            public void onTerminated() {
                System.out.println("DeleteTask subscription terminated");
            }

            @Override
            public void onConnected() {
                System.out.println("Connected to DeleteTask subscription");
            }
        });
    }

    public void subscribeToAddTask() {
        AddTaskSubscription addTaskSubscription = AddTaskSubscription
                .builder()
                .build();

        apolloClient.subscribe(addTaskSubscription).execute(new ApolloSubscriptionCall.Callback<AddTaskSubscription.Data>() {
            @Override
            public void onResponse(@NotNull Response<AddTaskSubscription.Data> response) {
                itemList.add(new Item(response.data().taskAdded.fragments().taskFields.title(), response.data().taskAdded.fragments().taskFields.description()
                        , response.data().taskAdded.fragments().taskFields.id()));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        itemAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                System.out.println("Failed " + e);
            }

            @Override
            public void onCompleted() {
                System.out.println("Subscribed to AddTask");
            }

            @Override
            public void onTerminated() {
                System.out.println("AddTask subscription terminated");
            }

            @Override
            public void onConnected() {
                System.out.println("Connected to AddTask subscription");
            }
        });

    }

    public void deleteTask(View view) {

        final Button button = view.findViewById(R.id.deleteButton);

        final String buttonId = button.getTag().toString();

        DeleteTaskMutation deleteTask = DeleteTaskMutation
                .builder()
                .id(buttonId)
                .build();

        apolloClient.mutate(deleteTask).enqueue(new ApolloCall.Callback<DeleteTaskMutation.Data>() {
            @Override
            public void onResponse(@NotNull final Response<DeleteTaskMutation.Data> response) {

                for (Item item : itemList) {
                    if (response.data() != null && item.getId().equals(response.data().deleteTask())) {
                        itemToRemove = item;
                    }
                }

                itemList.remove(itemToRemove);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        itemAdapter.notifyDataSetChanged();
                    }
                });

            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                System.out.println(e);
            }
        });
    }

    public void addTaskActivity(View view) {
        Intent launchActivity1 = new Intent(this, CreateTask.class);
        startActivity(launchActivity1);
    }

    public void getTasks() {

        AllTasksQuery tasksQuery = AllTasksQuery
                .builder()
                .build();

        apolloClient.query(tasksQuery).enqueue(new ApolloCall.Callback<AllTasksQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<AllTasksQuery.Data> response) {
                final AllTasksQuery.Data mResponse = response.data();

                final int dataLength = mResponse.allTasks().size();

                for (int i = 0; i < dataLength; i++) {
                    taskTitle = mResponse.allTasks().get(i).fragments().taskFields().title();
                    taskDescription = mResponse.allTasks().get(i).fragments().taskFields().description();
                    taskId = mResponse.allTasks().get(i).fragments().taskFields().id();
                    itemList.add(new Item(taskTitle, taskDescription, taskId));
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        itemAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                System.out.println(e);
            }
        });
    }

}
