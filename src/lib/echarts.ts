import { use, init, getMap, registerMap, type ECharts } from 'echarts/core'
import { BarChart, EffectScatterChart, LineChart, MapChart, PieChart, ScatterChart } from 'echarts/charts'
import { AriaComponent, GeoComponent, GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer, SVGRenderer } from 'echarts/renderers'
import type { EChartsOption } from 'echarts'

// 统一在这里注册当前项目实际使用的图表类型与组件，
// 避免 BaseEChart 和地图面板继续从 echarts 整包引入，影响构建体积和拆包效果。
use([
  BarChart,
  EffectScatterChart,
  LineChart,
  MapChart,
  PieChart,
  ScatterChart,
  AriaComponent,
  GeoComponent,
  GridComponent,
  LegendComponent,
  TooltipComponent,
  CanvasRenderer,
  SVGRenderer,
])

export { getMap, init, registerMap }
export type { ECharts, EChartsOption }
