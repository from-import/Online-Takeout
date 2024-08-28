import com.xxx.takeout.TakeoutApplication;
import com.xxx.takeout.common.CustomException;
import com.xxx.takeout.entity.Category;
import com.xxx.takeout.entity.Dish;
import com.xxx.takeout.entity.Setmeal;
import com.xxx.takeout.service.CategoryService;
import com.xxx.takeout.service.DishService;
import com.xxx.takeout.service.SetmealService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(classes = TakeoutApplication.class)
@ExtendWith(SpringExtension.class)
public class CategoryServiceImplTest {

    @Autowired
    private CategoryService categoryService;

    @MockBean
    private DishService dishService;

    @MockBean
    private SetmealService setmealService;



    @Test
    public void testRemoveCategoryWithDish() {
        // Setup mock behavior
        Mockito.when(dishService.count(Mockito.any())).thenReturn(1);
        Mockito.when(setmealService.count(Mockito.any())).thenReturn(0);

        // Test delete with associated dish
        CustomException exception = Assertions.assertThrows(CustomException.class, () -> {
            categoryService.remove(1L);
        });

        Assertions.assertEquals("本分类关联了菜品或套餐", exception.getMessage());
    }

    @Test
    public void testRemoveCategoryWithSetmeal() {
        // Setup mock behavior
        Mockito.when(dishService.count(Mockito.any())).thenReturn(0);
        Mockito.when(setmealService.count(Mockito.any())).thenReturn(1);

        // Test delete with associated setmeal
        CustomException exception = Assertions.assertThrows(CustomException.class, () -> {
            categoryService.remove(1L);
        });

        Assertions.assertEquals("本分类关联了菜品或套餐", exception.getMessage());
    }

    @Test
    public void testRemoveCategoryWithDishAndSetmeal() {
        // Setup mock behavior
        Mockito.when(dishService.count(Mockito.any())).thenReturn(1);
        Mockito.when(setmealService.count(Mockito.any())).thenReturn(1);

        // Test delete with associated dish and setmeal
        CustomException exception = Assertions.assertThrows(CustomException.class, () -> {
            categoryService.remove(1L);
        });

        Assertions.assertEquals("本分类关联了菜品或套餐", exception.getMessage());
    }
}
