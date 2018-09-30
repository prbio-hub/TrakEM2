
library(data.table)
library(dplyr)

# read csv files with fread to account for all separatores that may be used
segmentsData_rt <- fread("data/rhizoTrak_t23_segmentsAllLayers.csv")
aggregatedData_rt <- fread("data/rhizoTrak_t23_aggregatedAllLayers.csv")


for(i in unique(aggregatedData_rt$layerID))
{
  # filter by layer, group by status and summarise by metrics
  data <- segmentsData_rt %>% 
              filter(layerID == i) %>% 
              group_by(status) %>% 
              summarise(sum(`length_pixel`), sum(`surfaceArea_pixel^2`), sum(`volume_pixel^3`))
  
  metrics <- colnames(aggregatedData_rt)[5:7]
  data <- cbind(rep(i, length(unique(segmentsData_rt$status))), data)
  colnames(data) <- c("layerID", "status", paste0(metrics, "_sum"))

  # compare metrics for each status
  for(s in unique(data$status))
  {
    agg <- c(aggregatedData_rt[layerID==i & status==s, ]$`length_pixel`, 
             aggregatedData_rt[layerID==i & status==s, ]$`surfaceArea_pixel^2`,
             aggregatedData_rt[layerID==i & status==s, ]$`volume_pixel^3`)
    
    sum <- c(data[data$status==s, ]$`length_pixel_sum`, 
             data[data$status==s, ]$`surfaceArea_pixel^2_sum`, 
             data[data$status==s, ]$`volume_pixel^3_sum`)
    
    writeLines(paste0("Checking for status ", s, " in layer ", i, "..."))
    writeLines(paste0(metrics[1], "_agg", ": ", agg[1], 
                      ",\t", metrics[1], "_sum", ": ", sum[1]))
    writeLines(paste0(metrics[2], "_agg", ": ", agg[2], 
                      ",\t", metrics[2], "_sum", ": ", sum[2]))
    writeLines(paste0(metrics[3], "_agg", ": ", agg[3], 
                      ",\t", metrics[3], "_sum", ": ", sum[3]))
    
    if(isTRUE(all.equal(agg, sum)))
    {
      writeLines("\nCorrect!\n")
    }
    else
    {
      writeLines("\nFound discrepancies!\n")
    }

  }
}