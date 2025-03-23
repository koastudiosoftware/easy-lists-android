package com.easylists.presentation.common.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.easylists.presentation.ui.listdetails.ListDetailsViewModel
import com.toxicbakery.logging.Arbor

interface SelectableOption {
    val text: String
}


data class ComboOption(
    override val text: String,
    val id: Int,
) : SelectableOption


//region MultiSelectComboBox
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectComboBox(
    labelText: String,
    options: List<ComboOption>,
    onOptionsChosen: (List<ComboOption>) -> Unit,
    viewModel: ListDetailsViewModel,
    modifier: Modifier = Modifier,
    selectedIds: List<Int> = emptyList(),
) {
    var expanded = remember { mutableStateOf(false) }
    // when no options available, I want ComboBox to be disabled
//    val isEnabled = rememberUpdatedState { options.isNotEmpty() }
    var selectedOptionsList = remember { mutableStateListOf<Int>()}
    var firstTime = remember { mutableStateOf(true) }

    // initial setup of selected ids
    if (firstTime.value == true) {
        selectedIds.forEach {
            selectedOptionsList.add(it)
        }
        firstTime.value = false
    }

    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = {
            expanded.value = !expanded.value
            if (!expanded.value) {
                onOptionsChosen(options.filter { it.id in selectedOptionsList }.toList())
            }
        },
        modifier = modifier,
    ) {
        val selectedSummary = when (selectedOptionsList.size) {
//            0 -> ""
//            1 -> op
            //            tions.first { it.id == selectedOptionsList.first() }.text
            else -> "${selectedOptionsList.size} selected"
        }
        TextField(
            enabled = true,
            // The `menuAnchor` modifier must be passed to the text field to handle
            // expanding/collapsing the menu on click. A read-only text field has
            // the anchor type `PrimaryNotEditable`.
            modifier = modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            readOnly = true,
            value = selectedSummary,
            onValueChange = {},
            label = { Text(text = labelText) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = {
                expanded.value = false
                onOptionsChosen(options.filter { it.id in selectedOptionsList }.toList())
            },
        ) {
            for (option in options) {
                // use derivedStateOf to evaluate if it is checked
                var checked = remember {
                    derivedStateOf{option.id in selectedOptionsList}
                }.value

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { newCheckedState ->
                                    if (newCheckedState) {
                                        selectedOptionsList.add(option.id)
                                    } else {
                                        selectedOptionsList.remove(option.id)
                                    }
                                    viewModel.onSelectedIdsChange(selectedOptionsList)
                                },
                            )
                            Text(text = option.text)
                        }
                    },
                    onClick = {
                        if (!checked) {
                            selectedOptionsList.add(option.id)
                        } else {
                            selectedOptionsList.remove(option.id)
                        }
                        viewModel.onSelectedIdsChange(selectedOptionsList)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
//endregion