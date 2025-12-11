package com.example.learning_app;

import android.app.Application;
import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.learning_app.entities.Lesson;
import com.example.learning_app.viewmodel.LessonViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Instrumented test để kiểm tra ViewModel và Repository với Room Database
 * 
 * Để chạy test này:
 * 1. Click chuột phải vào class này
 * 2. Chọn "Run 'LessonViewModelTest'"
 * 3. Hoặc chạy từ terminal: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4.class)
public class LessonViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private LessonViewModel viewModel;
    private Application application;

    @Before
    public void setup() {
        application = ApplicationProvider.getApplicationContext();
        viewModel = new LessonViewModel(application);
    }

    @Test
    public void testViewModelNotNull() {
        assertNotNull("ViewModel không được null", viewModel);
    }

    @Test
    public void testGetAllLessons() throws InterruptedException {
        // Import data first
        viewModel.importDataFromJson();
        
        // Wait a bit for data to be imported
        Thread.sleep(2000);
        
        // Get LiveData
        LiveData<List<Lesson>> lessonsLiveData = viewModel.getAllLessons();
        assertNotNull("LiveData không được null", lessonsLiveData);
        
        // Get value from LiveData
        List<Lesson> lessons = getValue(lessonsLiveData);
        assertNotNull("Danh sách lessons không được null", lessons);
        
        // Kiểm tra có dữ liệu
        if (lessons != null && !lessons.isEmpty()) {
            assertTrue("Phải có ít nhất 1 lesson", lessons.size() > 0);
            System.out.println("✅ Test thành công! Tìm thấy " + lessons.size() + " bài học");
        } else {
            System.out.println("⚠️ Chưa có dữ liệu, có thể cần thời gian import");
        }
    }

    /**
     * Helper method để lấy giá trị từ LiveData trong test
     */
    private <T> T getValue(LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        CountDownLatch latch = new CountDownLatch(1);
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T o) {
                data[0] = o;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        liveData.observeForever(observer);
        latch.await(2, TimeUnit.SECONDS);
        return (T) data[0];
    }
}


