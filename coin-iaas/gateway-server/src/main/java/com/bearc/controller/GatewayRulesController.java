package com.bearc.controller;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class GatewayRulesController {
/*
* 获取所有网关限流配置信息
* */
    @GetMapping("/gateway")
    public Set<GatewayFlowRule> getGatewayFlowRules(){
        return GatewayRuleManager.getRules() ;
    }
    /*
     * 获取所有api分组信息
     * */
    @GetMapping("/api")
    public Set<ApiDefinition> getApiGroupRules(){
        return GatewayApiDefinitionManager.getApiDefinitions();
    }
}
