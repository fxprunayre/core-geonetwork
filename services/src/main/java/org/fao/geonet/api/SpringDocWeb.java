package org.fao.geonet.api;

import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.*;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springdoc.webmvc.api.ActuatorProvider;
import org.springdoc.webmvc.api.OpenApiResource;
import org.springdoc.webmvc.api.RouterFunctionProvider;
import org.springdoc.webmvc.core.RequestBuilder;
import org.springframework.boot.actuate.autoconfigure.web.server.ConditionalOnManagementPort;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.web.method.HandlerMethod;
//import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import java.util.List;
import java.util.Optional;

import static org.springdoc.core.Constants.SPRINGDOC_SHOW_ACTUATOR;

@Configuration
//@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
//@ConditionalOnProperty(name = SPRINGDOC_ENABLED, matchIfMissing = true)
public class SpringDocWeb {

        @Bean
//        @ConditionalOnMissingBean
        @Lazy(false)
        OpenApiResource openApiResource(OpenAPIBuilder openAPIBuilder, AbstractRequestBuilder requestBuilder,
                                        GenericResponseBuilder responseBuilder, OperationBuilder operationParser,
                                        RequestMappingInfoHandlerMapping requestMappingHandlerMapping,
                                        Optional<ActuatorProvider> servletContextProvider,
                                        SpringDocConfigProperties springDocConfigProperties,
                                        Optional<List<OperationCustomizer>> operationCustomizers,
                                        Optional<List<OpenApiCustomiser>> openApiCustomisers,
                                        Optional<SecurityOAuth2Provider> springSecurityOAuth2Provider,
                                        Optional<RouterFunctionProvider> routerFunctionProvider) {
            return new OpenApiResource(openAPIBuilder, requestBuilder,
                responseBuilder, operationParser,
                requestMappingHandlerMapping, servletContextProvider, operationCustomizers,
                openApiCustomisers, springDocConfigProperties, springSecurityOAuth2Provider,
                routerFunctionProvider);
        }

        @Bean
        @ConditionalOnMissingBean
        RequestBuilder requestBuilder(GenericParameterBuilder parameterBuilder, RequestBodyBuilder requestBodyBuilder,
                                      OperationBuilder operationBuilder, Optional<List<ParameterCustomizer>> parameterCustomizers,
                                      LocalVariableTableParameterNameDiscoverer localSpringDocParameterNameDiscoverer) {
            return new RequestBuilder(parameterBuilder, requestBodyBuilder,
                operationBuilder, parameterCustomizers, localSpringDocParameterNameDiscoverer);
        }

        @Bean
        @ConditionalOnMissingBean
        GenericResponseBuilder responseBuilder(OperationBuilder operationBuilder, List<ReturnTypeParser> returnTypeParsers, SpringDocConfigProperties springDocConfigProperties, PropertyResolverUtils propertyResolverUtils) {
            return new GenericResponseBuilder(operationBuilder, returnTypeParsers, springDocConfigProperties, propertyResolverUtils);
        }

//        @ConditionalOnClass(RouterFunction.class)
        class SpringDocWebMvcRouterConfiguration {

            @Bean
            @ConditionalOnMissingBean
            RouterFunctionProvider routerFunctionProvider(ApplicationContext applicationContext) {
                return new RouterFunctionProvider(applicationContext);
            }
        }

        @ConditionalOnProperty(SPRINGDOC_SHOW_ACTUATOR)
        @ConditionalOnClass(WebMvcEndpointHandlerMapping.class)
        @ConditionalOnManagementPort(ManagementPortType.SAME)
        class SpringDocWebMvcActuatorConfiguration {

            @Bean
            @ConditionalOnMissingBean
            ActuatorProvider actuatorProvider(WebMvcEndpointHandlerMapping webMvcEndpointHandlerMapping) {
                return new ActuatorProvider(webMvcEndpointHandlerMapping);
            }

            @Bean
            @Lazy(false)
            OperationCustomizer actuatorCustomizer(ActuatorProvider actuatorProvider) {
                return new OperationCustomizer() {
                    private int methodCount;

                    @Override
                    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
                        if (operation.getTags() != null && operation.getTags().contains(actuatorProvider.getTag().getName())) {
                            operation.setSummary(handlerMethod.toString());
                            operation.setOperationId(operation.getOperationId() + "_" + methodCount++);
                        }
                        return operation;
                    }
                };
            }

        }
    }
