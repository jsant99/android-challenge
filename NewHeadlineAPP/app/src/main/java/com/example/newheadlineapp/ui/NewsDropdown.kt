package com.example.newheadlineapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newheadlineapp.NewsViewModel
import com.example.newheadlineapp.newsSources
import kotlin.collections.component1
import kotlin.collections.component2

//###########################################################################################################
//                                            NewsDropdown                                                  #
// Composable for Dropdown menu for selecting news source when pressed.                                     #
// Displays Source title.                                                                                   #
//###########################################################################################################

@Composable
fun NewsDropdown(viewModel: NewsViewModel) {
    var expanded by remember { mutableStateOf(false) }

    //On first creation sets the first source as selected source to display as title
    var selectedSource by rememberSaveable {
        mutableStateOf(newsSources.keys.first())
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopCenter)
    ) {
        Row(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedSource,
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                modifier = Modifier
                    .weight(0.8f)
                    .padding(end = 8.dp),
                fontWeight = FontWeight.Bold,
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown arrow",
                modifier = Modifier.weight(0.2f).size(48.dp),
                tint = Color.White
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            newsSources.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(key,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)},
                    onClick = {
                        selectedSource = key
                        viewModel.onSelectedSourceChange(label)
                        expanded = false
                    }
                )
            }
        }
    }
}