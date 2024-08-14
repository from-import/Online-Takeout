package com.xxx.takeout.dto;

import com.xxx.takeout.entity.Setmeal;
import com.xxx.takeout.entity.SetmealDish;
import com.xxx.takeout.entity.Setmeal;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
